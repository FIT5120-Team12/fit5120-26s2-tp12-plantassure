"""
Iteration 1 data processing pipeline
Merges four sources: VicFlora + Advisory List + VBA + GBIF (iNaturalist)
"""
import re
import csv
import io
import json
import zipfile
from collections import defaultdict

import pandas as pd
import openpyxl
import shapefile  # pyshp
from sqlalchemy import create_engine
from config import DB_CONFIG


# Step 1: Scientific name normalization
def normalize_name(raw_name):
    """Strip the author abbreviation, keep only genus + species epithet
    (including hybrid markers), lowercase it to produce a match_key"""
    if raw_name is None:
        return None
    name = str(raw_name).strip()
    if not name:
        return None
    tokens = name.split()
    if not tokens:
        return None

    parts = [tokens[0]]
    idx = 1
    # Handle hybrid markers (x / X)
    if idx < len(tokens) and tokens[idx].lower() in ("×", "x"):
        parts.append("x")
        idx += 1
    if idx < len(tokens):
        parts.append(tokens[idx])

    key = " ".join(parts).lower()
    key = re.sub(r"[^a-z\s\-]", "", key)  # strip punctuation
    key = re.sub(r"\s+", " ", key).strip()
    return key if key else None



# Step 2a: VicFlora
def load_vicflora(path):
    df = pd.read_csv(path, dtype=str)
    df["match_key"] = df["scientific_name"].apply(normalize_name)
    df = df[df["match_key"].notna() & (df["match_key"] != "")]

    keep_cols = [
        "scientific_name", "vernacular_name", "family",
        "establishment_means", "degree_of_establishment", "match_key",
    ]
    df = df[keep_cols].drop_duplicates(subset="match_key", keep="first")
    return df.reset_index(drop=True)



# Step 2b: Advisory List (only take Risk Rating)
def load_advisory(path, sheet_name="Advisory list 2022"):
    wb = openpyxl.load_workbook(path, read_only=True, data_only=True)
    ws = wb[sheet_name]
    it = ws.iter_rows(values_only=True)
    header = next(it)
    idx_name = header.index("Scientific name")
    idx_risk = header.index("Risk Rating")

    records = []
    for row in it:
        name = row[idx_name]
        if name is None:
            continue
        key = normalize_name(name)
        if not key:
            continue
        risk = row[idx_risk]
        risk_clean = str(risk).strip() if risk is not None else None
        records.append({"match_key": key, "Risk Rating": risk_clean})

    df = pd.DataFrame(records).drop_duplicates(subset="match_key", keep="first")
    return df.reset_index(drop=True)



# Step 2c: VBA (aggregated by SCI_NAME)
def load_vba(shp_path):
    sf = shapefile.Reader(shp_path)
    field_names = [f[0] for f in sf.fields[1:]]  # skip DeletionFlag
    idx_name = field_names.index("SCI_NAME")
    idx_year = field_names.index("START_YEAR")

    agg = defaultdict(lambda: {"count": 0, "max_year": None})
    for rec in sf.iterRecords():
        name = rec[idx_name]
        if not name or not str(name).strip():
            continue
        key = normalize_name(name)
        if not key:
            continue
        year = rec[idx_year]
        a = agg[key]
        a["count"] += 1
        if isinstance(year, (int, float)) and (a["max_year"] is None or year > a["max_year"]):
            a["max_year"] = int(year)

    rows = [
        {"match_key": k, "vba_record_count": v["count"], "vba_most_recent_year": v["max_year"]}
        for k, v in agg.items()
    ]
    return pd.DataFrame(rows)



# Step 2d: GBIF/iNaturalist (aggregated by scientificName)
# Supports two input forms: a .zip archive or an already-extracted .csv file
def load_gbif(path):
    agg = defaultdict(lambda: {"count": 0, "max_date": None})

    if path.endswith(".zip"):
        z = zipfile.ZipFile(path)
        inner_name = [n for n in z.namelist() if n.endswith(".csv")][0]
        fh = io.TextIOWrapper(z.open(inner_name), encoding="utf-8", errors="replace")
    else:
        fh = open(path, "r", encoding="utf-8", errors="replace")

    with fh:
        reader = csv.DictReader(fh, delimiter="\t")
        for row in reader:
            name = row.get("scientificName")
            if not name or not name.strip():
                continue
            key = normalize_name(name)
            if not key:
                continue
            date = row.get("eventDate") or None
            a = agg[key]
            a["count"] += 1
            if date and (a["max_date"] is None or date > a["max_date"]):
                a["max_date"] = date

    rows = [
        {"match_key": k, "inat_record_count": v["count"], "inat_most_recent_date": v["max_date"]}
        for k, v in agg.items()
    ]
    return pd.DataFrame(rows)



# Step 6: Rule engine (only takes Risk Rating as input)
def apply_rule(risk_rating):
    if risk_rating is None or risk_rating == "" or risk_rating == "Not Assessed / No exact match":
        return "Not Assessed"
    rr = risk_rating.lower()
    if "very high" in rr or "high" in rr:  # covers Very High Risk / High Risk / Moderately High Risk
        return "Reconsider Planting"
    if "medium" in rr:
        return "Use Caution"
    return "Lower Concern"  # covers Lower Risk / Potential Risk



# Main pipeline
def run_pipeline(vicflora_path, advisory_path, vba_shp_path, gbif_path):
    print("Step 2a: Reading VicFlora ...")
    vf = load_vicflora(vicflora_path)
    print(f"  -> {len(vf)} records")

    print("Step 2b: Reading Advisory List ...")
    adv = load_advisory(advisory_path)
    print(f"  -> {len(adv)} records")

    print("Step 2c: Reading and aggregating VBA ...")
    vba = load_vba(vba_shp_path)
    print(f"  -> {len(vba)} species")

    print("Step 2d: Reading and aggregating GBIF/iNaturalist (large file, this will take a moment) ...")
    inat = load_gbif(gbif_path)
    print(f"  -> {len(inat)} species")

    print("Step 3: Left-joining ...")
    merged = vf.merge(adv, on="match_key", how="left")
    merged = merged.merge(vba, on="match_key", how="left")
    merged = merged.merge(inat, on="match_key", how="left")

    print("Step 4: Filling in missing values ...")
    merged["Risk Rating"] = merged["Risk Rating"].fillna("Not Assessed / No exact match")
    merged["establishment_means"] = merged["establishment_means"].fillna("Not available")
    merged["degree_of_establishment"] = merged["degree_of_establishment"].fillna("Not available")

    merged["vba_most_recent_year"] = merged["vba_most_recent_year"].astype(object)
    merged["inat_most_recent_date"] = merged["inat_most_recent_date"].astype(object)

    no_vba = merged["vba_record_count"].isna()
    merged.loc[no_vba, "vba_record_count"] = 0
    merged.loc[no_vba, "vba_most_recent_year"] = "No local records found"
    merged.loc[~no_vba, "vba_record_count"] = merged.loc[~no_vba, "vba_record_count"].astype(int)
    merged.loc[~no_vba, "vba_most_recent_year"] = merged.loc[~no_vba, "vba_most_recent_year"].apply(
        lambda v: int(v) if pd.notna(v) else v
    )

    no_inat = merged["inat_record_count"].isna()
    merged.loc[no_inat, "inat_record_count"] = 0
    merged.loc[no_inat, "inat_most_recent_date"] = "No local records found"
    merged.loc[~no_inat, "inat_record_count"] = merged.loc[~no_inat, "inat_record_count"].astype(int)

    print("Step 6+7: Computing rules + building final output structure ...")
    output = []
    for _, row in merged.iterrows():
        output.append({
            "scientific_name": row["scientific_name"],
            "common_name": row["vernacular_name"],
            "recommendation": apply_rule(row["Risk Rating"]),
            "supporting_evidence": {
                "establishment_status": row["establishment_means"],
                "degree_of_establishment": row["degree_of_establishment"],
                "vba_record_count": int(row["vba_record_count"]),
                "vba_most_recent_year": row["vba_most_recent_year"],
                "inat_record_count": int(row["inat_record_count"]),
                "inat_most_recent_date": row["inat_most_recent_date"],
            },
            "data_sources": ["VicFlora", "2022 Advisory List", "VBA Flora Records", "iNaturalist (GBIF)"],
        })

    return output, merged



# Write to MySQL (read directly by main.py's API)
def write_to_mysql(merged_df):
    engine = create_engine(
        f"mysql+pymysql://{DB_CONFIG['user']}:{DB_CONFIG['password']}"
        f"@{DB_CONFIG['host']}:{DB_CONFIG['port']}/{DB_CONFIG['database']}"
    )
    # if_exists="replace": every pipeline run overwrites the whole table with the latest data
    merged_df.to_sql("species_data", con=engine, if_exists="replace", index=False)
    print(f"  -> Written to MySQL table species_data, {len(merged_df)} rows total")


if __name__ == "__main__":
    result, merged_df = run_pipeline(
        vicflora_path="data/vicflora_monash_2026.csv",
        advisory_path="data/Advisory-list-of-environmental-weeds-in-Victoria_2022.xlsx",
        vba_shp_path="data/Order_ELFIES/mga2020_55/esrishape/lga_polygon/MONASH-0/FLORAFAUNA1/VBA_FLORA25.shp",
        gbif_path="data/iNaturalist.csv",
    )

    with open("output.json", "w", encoding="utf-8") as f:
        json.dump(result, f, ensure_ascii=False, indent=2)

    merged_df.to_csv("output.csv", index=False, encoding="utf-8-sig")

    print(f"\nDone! {len(result)} species total")
    print("recommendation distribution:")
    from collections import Counter
    print(Counter(r["recommendation"] for r in result))

    print("\nStep 8: Writing to MySQL ...")
    try:
        write_to_mysql(merged_df)
    except Exception as e:
        print(f"  -> MySQL write failed (output.json/output.csv were still generated): {e}")
        print("  -> Please check the database config in config.py, and whether the MySQL service is running")