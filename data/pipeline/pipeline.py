"""
Iteration 1 data processing pipeline.
Merges four sources: VicFlora + Advisory List + VBA + GBIF (iNaturalist).

Two match keys are used to join species names across datasets:
  - specific_key: genus + species + subspecies/variety/cultivar, when
    present. Used to join VicFlora <-> Advisory List.
  - species_key: genus + species only. Used to join occurrence data
    (VBA, iNaturalist).
"""
import re  # need this for cleaning up the species name strings
import csv  # for reading the plain-text GBIF/iNaturalist file
import io  # lets us treat the zip's inner file like a normal text file
import json  # to dump the final result as a .json file
import zipfile  # in case the GBIF data still comes as a .zip
from collections import defaultdict  # handy for counting/aggregating without KeyErrors

import pandas as pd  # our main tool for tables/dataframes
import openpyxl  # reads the Advisory List .xlsx file
import shapefile  # pyshp, reads the VBA .shp file
from sqlalchemy import create_engine  # lets us talk to MySQL
from config import DB_CONFIG  # our db login info lives in config.py

# Step 1: Name normalization
_INFRA_MARKERS = {"subsp", "ssp", "var", "f", "forma"}  # words that signal "there's a subspecies/variety here"


def normalize_name_species(raw_name):
    """
    Species-level key: genus + species epithet only (handles hybrid
    markers ×/x). Used for joining occurrence datasets (VBA,
    iNaturalist).
    """
    if raw_name is None:  # nothing to work with, bail out
        return None
    name = str(raw_name).strip()  # force to string and trim whitespace just in case
    if not name:  # empty string after stripping, still nothing to work with
        return None
    tokens = name.split()  # split "Acacia longifolia" into ["Acacia", "longifolia"]
    if not tokens:  # split gave us nothing (shouldn't really happen but be safe)
        return None

    parts = [tokens[0]]  # start building our key with the genus
    idx = 1  # pointer to the next word we're looking at
    # Handle hybrid marker (× / x / X)
    if idx < len(tokens) and tokens[idx].lower() in ("×", "x"):  # check if next word is a hybrid symbol
        parts.append("x")  # normalise it to a plain "x"
        idx += 1  # move past it
    if idx < len(tokens):  # if there's still a word left, it's the species epithet
        parts.append(tokens[idx])  # add it to our key

    key = " ".join(parts).lower()  # glue the parts together and lowercase everything
    key = re.sub(r"[^a-z\s\-]", "", key)  # strip punctuation
    key = re.sub(r"\s+", " ", key).strip()  # collapse any double spaces, trim edges
    return key if key else None  # return None instead of an empty string


def normalize_name_specific(raw_name):
    """
    Subspecies-aware key: genus + species epithet + subspecies/variety/
    forma epithet when present (e.g. "subsp. orientalis", "var. alba"),
    or the full cultivar name when the species position is a quoted
    cultivar (e.g. Grevillea 'Poorinda Constance'). Falls back to the
    species-level key when no infraspecific rank is present.

    Used for joining VicFlora <-> Advisory List.
    """
    if raw_name is None:  # same guard as above, nothing to do
        return None
    name = str(raw_name).strip()  # clean up the input
    if not name:  # empty after stripping
        return None
    tokens = name.split()  # break the name into words
    if not tokens:  # nothing came out of the split
        return None

    parts = [tokens[0]]  # genus  -- first word always goes in
    idx = 1  # index of the word we're about to look at

    # Handle hybrid marker (× / x / X)
    if idx < len(tokens) and tokens[idx].lower() in ("×", "x"):  # is the 2nd word a hybrid marker?
        parts.append("x")  # keep it, normalised
        idx += 1  # skip past it

    if idx >= len(tokens):  # only a genus, nothing else in the name
        key = " ".join(parts).lower()  # just build the key from what we have
        key = re.sub(r"[^a-z\s\-]", "", key)  # strip out punctuation
        key = re.sub(r"\s+", " ", key).strip()  # tidy up spacing
        return key if key else None  # bail with None if somehow empty

    # Cultivar name in quotes (e.g. Grevillea 'Poorinda Constance')
    # -> keep every remaining token so distinct cultivars don't collide.
    if tokens[idx].startswith(("'", '"', "\u2018", "\u2019")):  # looks like a quoted cultivar name
        parts.extend(tokens[idx:])  # just keep everything from here on, don't lose any words
    else:  # normal case, not a cultivar
        parts.append(tokens[idx])  # species epithet -- grab the next word
        idx += 1  # move on to check what's after it
        # Check for an infraspecific rank marker (subsp./var./f./forma)
        if idx < len(tokens):  # is there a word after the species epithet?
            marker = tokens[idx].lower().rstrip(".")  # lowercase it and drop a trailing dot (e.g. "subsp." -> "subsp")
            if marker in _INFRA_MARKERS:  # is it actually one of our known subspecies/variety markers?
                parts.append("subsp")  # normalise all markers to one form
                idx += 1  # move past the marker word
                if idx < len(tokens):  # is there an epithet after the marker?
                    parts.append(tokens[idx])  # infraspecific epithet -- e.g. "europaea" in "subsp. europaea"

    key = " ".join(parts).lower()  # join everything into one string, lowercase
    key = re.sub(r"[^a-z\s\-]", "", key)  # strip punctuation
    key = re.sub(r"\s+", " ", key).strip()  # squash extra whitespace
    return key if key else None  # return None if we somehow ended up with nothing


# Step 2a: VicFlora
def load_vicflora(path):
    df = pd.read_csv(path, dtype=str)  # read the csv, keep everything as text so nothing gets auto-converted weirdly
    df["specific_key"] = df["scientific_name"].apply(normalize_name_specific)  # build the subspecies-aware key for every row
    df["species_key"] = df["scientific_name"].apply(normalize_name_species)  # also build the species-only key (fallback/join to VBA & iNat)
    df = df[df["specific_key"].notna() & (df["specific_key"] != "")]  # drop rows where we couldn't make a usable key at all

    keep_cols = [  # only keep the columns we actually care about downstream
        "scientific_name", "vernacular_name", "family",
        "establishment_means", "degree_of_establishment",
        "specific_key", "species_key",
    ]
    df = df[keep_cols].drop_duplicates(subset="specific_key", keep="first")  # trim to those cols, drop exact-key duplicates (keep the first one seen)
    return df.reset_index(drop=True)  # tidy up the row numbering before handing it back


# Step 2b: Advisory List (Risk Rating only)
def load_advisory(path, sheet_name="Advisory list 2022"):
    wb = openpyxl.load_workbook(path, read_only=True, data_only=True)  # open the xlsx, read-only for speed, data_only to get values not formulas
    ws = wb[sheet_name]  # grab the specific sheet we need
    it = ws.iter_rows(values_only=True)  # an iterator over rows, just the raw values (no cell objects)
    header = next(it)  # pull off the first row as the header
    idx_name = header.index("Scientific name")  # find which column position has the name
    idx_risk = header.index("Risk Rating")  # and which one has the risk rating

    records = []  # we'll collect cleaned-up rows here
    for row in it:  # go through every remaining (non-header) row
        name = row[idx_name]  # grab the scientific name cell
        if name is None:  # skip blank rows
            continue
        key = normalize_name_specific(name)  # turn it into our subspecies-aware key
        if not key:  # couldn't build a key, skip this row
            continue
        risk = row[idx_risk]  # grab the risk rating cell
        risk_clean = str(risk).strip() if risk is not None else None  # stringify + trim, or leave as None if empty
        records.append({"specific_key": key, "risk_rating": risk_clean})  # stash the cleaned pair

    df = pd.DataFrame(records).drop_duplicates(subset="specific_key", keep="first")  # turn into a dataframe, drop exact dupes (first wins)
    return df.reset_index(drop=True)  # reset row index before returning


# Step 2c: VBA (aggregated by SCI_NAME, species-level)
def load_vba(shp_path):
    sf = shapefile.Reader(shp_path)  # open the shapefile
    field_names = [f[0] for f in sf.fields[1:]]  # skip DeletionFlag  -- pyshp always sticks this dummy field first
    idx_name = field_names.index("SCI_NAME")  # find the column with the species name
    idx_year = field_names.index("START_YEAR")  # find the column with the observation year

    agg = defaultdict(lambda: {"count": 0, "max_year": None})  # running totals per species key, auto-creates on first use
    for rec in sf.iterRecords():  # loop through every record in the shapefile
        name = rec[idx_name]  # pull out this record's species name
        if not name or not str(name).strip():  # skip blank/missing names
            continue
        key = normalize_name_species(name)  # collapse it down to the species-only key
        if not key:  # couldn't normalise it, skip
            continue
        year = rec[idx_year]  # pull out the year for this record
        a = agg[key]  # get (or create) the running totals bucket for this species
        a["count"] += 1  # bump the record count
        if isinstance(year, (int, float)) and (a["max_year"] is None or year > a["max_year"]):  # only update if year is a real number and newer than what we have
            a["max_year"] = int(year)  # store it as a plain int

    rows = [  # flatten the aggregation dict into a list of row-dicts
        {"species_key": k, "vba_record_count": v["count"], "vba_most_recent_year": v["max_year"]}
        for k, v in agg.items()
    ]
    return pd.DataFrame(rows)  # wrap it up as a dataframe


# Step 2d: GBIF/iNaturalist (aggregated by scientificName, species-level)
# Supports either a .zip archive or an already-extracted .csv file.
def load_gbif(path):
    agg = defaultdict(lambda: {"count": 0, "max_date": None})  # same pattern as VBA -- running totals per species

    if path.endswith(".zip"):  # if we were handed the raw zip download
        z = zipfile.ZipFile(path)  # open it
        inner_name = [n for n in z.namelist() if n.endswith(".csv")][0]  # find the one csv file inside it
        fh = io.TextIOWrapper(z.open(inner_name), encoding="utf-8", errors="replace")  # open it as text, replacing any bad bytes
    else:  # otherwise assume it's already an extracted plain csv
        fh = open(path, "r", encoding="utf-8", errors="replace")  # just open it directly

    with fh:  # make sure the file handle gets closed properly either way
        reader = csv.DictReader(fh, delimiter="\t")  # GBIF exports are tab-separated, not comma
        for row in reader:  # go row by row (this file can be huge, so we stream it)
            name = row.get("scientificName")  # grab the name field
            if not name or not name.strip():  # skip blank names
                continue
            key = normalize_name_species(name)  # collapse to species-only key
            if not key:  # couldn't normalise, skip
                continue
            date = row.get("eventDate") or None  # grab the observation date, or None if missing
            a = agg[key]  # get/create the running totals for this species
            a["count"] += 1  # bump the count
            if date and (a["max_date"] is None or date > a["max_date"]):  # ISO dates sort fine as plain strings, so just compare them
                a["max_date"] = date  # keep the most recent one we've seen

    rows = [  # flatten into a list of dicts, same as VBA did
        {"species_key": k, "inat_record_count": v["count"], "inat_most_recent_date": v["max_date"]}
        for k, v in agg.items()
    ]
    return pd.DataFrame(rows)  # hand back as a dataframe



# Step 6: Rule engine (takes only Risk Rating as input)
#
# Verdict mapping, per the Iteration 1 API Interface Document Section 4.1:
#   Very High / High        -> Reconsider Planting
#   Moderately High / Medium -> Use Caution
#   Lower / Potential        -> Lower Concern
#   No exact assessment      -> Not Assessed
def apply_rule(risk_rating):
    if risk_rating is None or risk_rating == "" or risk_rating == "Not Assessed / No exact match":  # no real rating to work with
        return "Not Assessed"
    rr = risk_rating.lower()  # lowercase so we can do simple substring checks
    if "very high" in rr:  # catches "Very High Risk"
        return "Reconsider Planting"
    if "moderately high" in rr:  # catches "Moderately High Risk" -- check this BEFORE plain "high" below
        return "Use Caution"
    if "high" in rr:  # plain "High Risk"  -- anything left with "high" in it that isn't the two cases above
        return "Reconsider Planting"
    if "medium" in rr:  # catches "Medium Risk"
        return "Use Caution"
    return "Lower Concern"  # Lower Risk / Potential Risk  -- everything else falls here



# Main pipeline
def run_pipeline(vicflora_path, advisory_path, vba_shp_path, gbif_path):
    print("Step 2a: Loading VicFlora ...")  # just a progress message so we can see it's alive
    vf = load_vicflora(vicflora_path)  # actually load + clean VicFlora
    print(f"  -> {len(vf)} rows")  # report how many rows we ended up with

    print("Step 2b: Loading Advisory List ...")  # progress message
    adv = load_advisory(advisory_path)  # load + clean the Advisory List
    print(f"  -> {len(adv)} rows")  # report row count

    print("Step 2c: Loading and aggregating VBA ...")  # progress message
    vba = load_vba(vba_shp_path)  # load + aggregate VBA records
    print(f"  -> {len(vba)} species")  # report how many species we got

    print("Step 2d: Loading and aggregating GBIF/iNaturalist (large file, may take a moment) ...")  # heads-up this one's slower
    inat = load_gbif(gbif_path)  # load + aggregate the GBIF/iNaturalist data
    print(f"  -> {len(inat)} species")  # report species count

    print("Step 3: Joining datasets ...")  # progress message
    merged = vf.merge(adv, on="specific_key", how="left")  # bring in Risk Rating, matched at the subspecies level
    merged = merged.merge(vba, on="species_key", how="left")  # bring in VBA counts, matched at species level
    merged = merged.merge(inat, on="species_key", how="left")  # bring in iNaturalist counts, also species level

    # Only one key column is exposed downstream, matching the existing
    # species_data schema (match_key). specific_key becomes match_key;
    # species_key was only needed to join occurrence data and is dropped.
    merged = merged.drop(columns=["species_key"])  # don't need this anymore now that the joins are done
    merged = merged.rename(columns={"specific_key": "match_key"})  # rename to match the column name the rest of the system expects

    print("Step 4: Filling missing values ...")  # progress message
    merged["risk_rating"] = merged["risk_rating"].fillna("Not Assessed / No exact match")  # no Advisory List match -> say so explicitly
    merged["establishment_means"] = merged["establishment_means"].fillna("Not available")  # same idea for this field
    merged["degree_of_establishment"] = merged["degree_of_establishment"].fillna("Not available")  # and this one

    merged["vba_most_recent_year"] = merged["vba_most_recent_year"].astype(object)  # switch to object dtype so we can mix ints and None freely
    merged["inat_most_recent_date"] = merged["inat_most_recent_date"].astype(object)  # same reason for this column

    no_vba = merged["vba_record_count"].isna()  # rows where the VBA join didn't find anything
    merged.loc[no_vba, "vba_record_count"] = 0  # no records found -> just say zero
    # Keep this column as a true NULL (not a string) so it matches the
    # SMALLINT UNSIGNED NULL type in the species_data schema.
    merged.loc[~no_vba, "vba_record_count"] = merged.loc[~no_vba, "vba_record_count"].astype(int)  # for rows that DID match, force to a clean int
    merged.loc[~no_vba, "vba_most_recent_year"] = merged.loc[~no_vba, "vba_most_recent_year"].apply(
        lambda v: int(v) if pd.notna(v) else v  # tidy the year up into a plain int where we actually have one
    )

    no_inat = merged["inat_record_count"].isna()  # rows where the iNaturalist join didn't find anything
    merged.loc[no_inat, "inat_record_count"] = 0  # no records -> zero
    # Keep this column as a true NULL (not a string) so it matches the
    # DATETIME NULL type in the species_data schema.
    merged.loc[~no_inat, "inat_record_count"] = merged.loc[~no_inat, "inat_record_count"].astype(int)  # force matched rows to a clean int

    print("Step 6+7: Applying rule engine and building final output ...")  # progress message
    output = []  # this will hold our final list of species dicts
    for _, row in merged.iterrows():  # walk through every row of the merged table
        output.append({  # build the final nested dict shape for this species
            "scientific_name": row["scientific_name"],  # the full name
            "common_name": row["vernacular_name"],  # everyday name, if there is one
            "recommendation": apply_rule(row["risk_rating"]),  # run the rule engine to get a verdict
            "supporting_evidence": {  # everything below here is just context, not used for the decision itself
                "establishment_status": row["establishment_means"],
                "degree_of_establishment": row["degree_of_establishment"],
                "vba_record_count": int(row["vba_record_count"]),  # force to int in case pandas left it as a float
                "vba_most_recent_year": row["vba_most_recent_year"] if pd.notna(row["vba_most_recent_year"]) else "No local records found",  # human-readable fallback text
                "inat_record_count": int(row["inat_record_count"]),  # same int-forcing as above
                "inat_most_recent_date": row["inat_most_recent_date"] if pd.notna(row["inat_most_recent_date"]) else "No local records found",  # same fallback pattern
            },
            "data_sources": ["VicFlora", "2022 Advisory List", "VBA Flora Records", "iNaturalist (GBIF)"],  # just a fixed label list, always the same 4 sources
        })

    return output, merged  # hand back both the pretty json-ready list AND the raw merged dataframe (the latter is used for the MySQL write)


# Write to MySQL (read directly by main.py's API)
def write_to_mysql(merged_df):
    from sqlalchemy import Integer, SmallInteger, String, DateTime  # explicit column types, so the table isn't all TEXT

    engine = create_engine(
        f"mysql+pymysql://{DB_CONFIG['user']}:{DB_CONFIG['password']}"
        f"@{DB_CONFIG['host']}:{DB_CONFIG['port']}/{DB_CONFIG['database']}"
    )

    # Give every column a real type instead of letting pandas guess (it
    # tends to fall back to TEXT for anything with mixed/None values,
    # which makes backend validation harder downstream).
    column_types = {
        "scientific_name": String(150),
        "vernacular_name": String(200),
        "family": String(50),
        "establishment_means": String(30),
        "degree_of_establishment": String(30),
        "match_key": String(150),
        "risk_rating": String(50),
        "vba_record_count": SmallInteger,
        "vba_most_recent_year": SmallInteger,
        "inat_record_count": Integer,
        "inat_most_recent_date": DateTime,
    }

    # inat_most_recent_date is stored as an ISO string like
    # "2026-08-09T08:42:51" for the JSON/CSV output, but a real
    # DATETIME column needs an actual datetime value (and MySQL wants
    # a space instead of the "T"), so convert a copy just for the DB write.
    df_for_db = merged_df.copy()
    df_for_db["inat_most_recent_date"] = pd.to_datetime(
        df_for_db["inat_most_recent_date"], errors="coerce"
    )

    # if_exists="replace": every pipeline run overwrites the table with fresh data
    df_for_db.to_sql(
        "species_data", con=engine, if_exists="replace", index=False,
        dtype=column_types,
    )
    print(f"  -> Written to MySQL table species_data, {len(merged_df)} rows")


if __name__ == "__main__":  # only run all this if the script is executed directly, not imported
    result, merged_df = run_pipeline(  # kick off the whole pipeline with our actual file paths
        vicflora_path="data/vicflora_monash_2026.csv",
        advisory_path="data/Advisory-list-of-environmental-weeds-in-Victoria_2022.xlsx",
        vba_shp_path="data/Order_ELFIES/mga2020_55/esrishape/lga_polygon/MONASH-0/FLORAFAUNA1/VBA_FLORA25.shp",
        gbif_path="data/iNaturalist.csv",
    )

    with open("output.json", "w", encoding="utf-8") as f:  # open the output file for writing
        json.dump(result, f, ensure_ascii=False, indent=2)  # dump the result list as pretty-printed json

    merged_df.to_csv("output.csv", index=False, encoding="utf-8-sig")  # also save a csv version (utf-8-sig so Excel shows Chinese/special chars right)

    print(f"\nDone! {len(result)} species processed")  # quick summary line
    print("recommendation distribution:")  # heads-up for the next line
    from collections import Counter  # local import, just for this one-off tally
    print(Counter(r["recommendation"] for r in result))  # count how many species landed in each recommendation bucket

    print("\nStep 8: Writing to MySQL ...")  # progress message
    try:
        write_to_mysql(merged_df)  # try to push the data into MySQL
    except Exception as e:  # if MySQL isn't running or config is wrong, don't crash the whole script
        print(f"  -> MySQL write failed (output.json/output.csv were still generated): {e}")  # explain what happened
        print("  -> Check the database settings in config.py, and that the MySQL service is running")  # give a hint on how to fix it