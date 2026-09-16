"""
Iteration 2 Data Processing Pipeline -- Epic 2: Find a Better Plant + Compare

Inputs: VicFlora + 2022 Advisory List (reuses I1 logic) + AusTraits v7.0.0
        + GRIIS Australia + VBA_FLORA100 + ALA Monash occurrence records
"""
import re
import json
import os
from pathlib import Path
from collections import defaultdict, Counter

import pandas as pd
import openpyxl
import shapefile  # pyshp
import pyarrow.parquet as pq


import sys
SCRIPT_DIR = Path(__file__).resolve().parent
if len(sys.argv) > 1:
    BASE = sys.argv[1]
elif os.environ.get("PLANTASSURE_DATA_DIR"):
    BASE = os.environ["PLANTASSURE_DATA_DIR"]
else:
    BASE = str(SCRIPT_DIR / "data")


# Step 1: Scientific-name normalisation (identical to I1)
def normalize_name(raw_name):
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
    if idx < len(tokens) and tokens[idx].lower() in ("×", "x"):
        parts.append("x")
        idx += 1
    if idx < len(tokens):
        parts.append(tokens[idx])
    key = " ".join(parts).lower()
    key = re.sub(r"[^a-z\s\-]", "", key)
    key = re.sub(r"\s+", " ", key).strip()
    return key if key else None


# Step 2a: VicFlora (same as I1)
def load_vicflora(path):
    df = pd.read_csv(path, dtype=str)
    df["match_key"] = df["scientific_name"].apply(normalize_name)
    df = df[df["match_key"].notna() & (df["match_key"] != "")]
    keep_cols = [
        "scientific_name", "vernacular_name", "family",
        "establishment_means", "degree_of_establishment", "match_key",
    ]
    # the base table is NOT de-duplicated on match_key. Different
    # subspecies/varieties of the same species in VicFlora (e.g.
    # Acacia longifolia subsp. longifolia / subsp. sophorae) share the
    # same match_key (genus + species epithet only), but they are
    # distinct records in the source data and are all kept here.
    # When external sources (Advisory List/AusTraits/etc.) are joined
    # below, multiple subspecies sharing one match_key will receive the
    # same external values -- this is a known simplification, because
    # most external sources are only resolved to species level, not
    # subspecies level.
    df = df[keep_cols].reset_index(drop=True)
    return df


# Step 2b: Advisory List (same as I1, only Risk Rating is kept)
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


# Step 6: Rule engine (same as I1)
def apply_rule(risk_rating):
    if risk_rating is None or risk_rating == "" or risk_rating == "Not Assessed / No exact match":
        return "Not Assessed"
    rr = risk_rating.lower()
    # "Moderately High" and "Medium" must be checked BEFORE the generic
    # "high" check below -- "moderately high" contains the substring
    # "high", so checking "very high"/"high" first would incorrectly
    # catch "Moderately High Risk" and return "Reconsider Planting"
    # instead of the agreed "Use Caution". Order matters here.
    if "moderately high" in rr or "medium" in rr:
        return "Use Caution"
    if "very high" in rr or "high" in rr:
        return "Reconsider Planting"
    return "Lower Concern"


# Step 2c: AusTraits -- long format pivoted to wide format
# Categorical fields (growth_form/woodiness/life_history): take the mode;
#   ties are broken in favour of PREFERRED_DATASET.
# Numeric field (height): take the union of the observed range (min-max).

TARGET_TRAITS = {"plant_growth_form", "woodiness_detailed", "life_history", "plant_height"}
# Per-trait tie-break preference, per the validated design: Wenk_2022 is
# preferred for growth form and woodiness specifically; Wenk_2023 is
# preferred for life history specifically. A single global constant here
# would incorrectly apply Wenk_2023 to growth form and woodiness too.
PREFERRED_DATASET_BY_TRAIT = {
    "plant_growth_form": "Wenk_2022",
    "woodiness_detailed": "Wenk_2022",
    "life_history": "Wenk_2023",
}

# woodiness_detailed has 7 fine-grained categories (per AusTraits' own
# trait definitions: herbaceous, semi_woody, woody, woody_base,
# woody_like_inflorescence, woody_like_stem, woody_root). Exact-set
# matching on all 7 would be too strict for alternative-plant matching
# (e.g. "woody" would not match "woody_base" even though both are
# essentially woody plants), so we collapse them into 3 coarser buckets
# before comparing -- similar to the old binary woody/herbaceous split,
# but backed by data that actually has ~30k taxa of coverage instead of
# ~4k for the old raw "woodiness" trait.
WOODINESS_GROUPS = {
    "herbaceous": "herbaceous",
    "semi_woody": "semi_woody",
    "woody": "woody",
    "woody_base": "woody",
    "woody_like_inflorescence": "woody",
    "woody_like_stem": "woody",
    "woody_root": "woody",
}


def simplify_woodiness(value):
    """Collapse woodiness_detailed's 7 categories into 3 coarser buckets
    (herbaceous / semi_woody / woody) for matching purposes. Handles
    multi-value strings (e.g. "woody woody_base") by mapping each token
    and de-duplicating."""
    if value is None:
        return None
    tokens = str(value).split()
    mapped = {WOODINESS_GROUPS.get(t, t) for t in tokens}
    return " ".join(sorted(mapped))


def load_austraits_traits(parquet_path):
    pf = pq.ParquetFile(parquet_path)
    # taxon -> trait -> list of (value, dataset_id)
    cat_obs = defaultdict(lambda: defaultdict(list))
    height_obs = defaultdict(list)

    for batch in pf.iter_batches(
        batch_size=300000,
        columns=["taxon_name", "trait_name", "value", "dataset_id"],
    ):
        taxon = batch.column("taxon_name").to_pylist()
        trait = batch.column("trait_name").to_pylist()
        value = batch.column("value").to_pylist()
        dsid = batch.column("dataset_id").to_pylist()
        for i in range(len(taxon)):
            t = trait[i]
            if t not in TARGET_TRAITS:
                continue
            key = normalize_name(taxon[i])
            if not key or not value[i]:
                continue
            if t == "plant_height":
                try:
                    h = float(value[i])
                    height_obs[key].append(h)
                except (ValueError, TypeError):
                    continue
            else:
                cat_obs[key][t].append((value[i], dsid[i]))

    def resolve_categorical(observations, trait_name):
        """Mode; ties are broken in favour of that trait's preferred
        dataset per PREFERRED_DATASET_BY_TRAIT."""
        if not observations:
            return None
        counts = Counter(v for v, _ in observations)
        max_count = max(counts.values())
        tied = [v for v, c in counts.items() if c == max_count]
        if len(tied) == 1:
            return tied[0]
        preferred = PREFERRED_DATASET_BY_TRAIT.get(trait_name)
        for v, ds in observations:
            if v in tied and ds == preferred:
                return v
        return tied[0]

    rows = []
    all_keys = set(cat_obs.keys()) | set(height_obs.keys())
    for key in all_keys:
        gf = resolve_categorical(cat_obs[key].get("plant_growth_form", []), "plant_growth_form")
        wd_raw = resolve_categorical(cat_obs[key].get("woodiness_detailed", []), "woodiness_detailed")
        wd = simplify_woodiness(wd_raw)
        lh = resolve_categorical(cat_obs[key].get("life_history", []), "life_history")
        heights = height_obs.get(key, [])
        h_min = min(heights) if heights else None
        h_max = max(heights) if heights else None
        rows.append({
            "match_key": key,
            "growth_form": gf,
            "woodiness": wd,
            "life_history": lh,
            "height_min": h_min,
            "height_max": h_max,
        })
    return pd.DataFrame(rows)


# Step 2d: GRIIS Australia -- only whether a species is listed
# (used as a supplementary-evidence flag).
# Rule: this flag is only surfaced in the display layer when VicFlora
# already classifies the species as Introduced in Victoria.

def load_griis(dwca_dir):
    taxon = pd.read_csv(f"{dwca_dir}/taxon.txt", sep="\t", dtype=str)
    profile = pd.read_csv(f"{dwca_dir}/speciesprofile.txt", sep="\t", dtype=str)
    merged = taxon.merge(profile, on="id", how="left")
    merged["match_key"] = merged["scientificName"].apply(normalize_name)
    merged = merged[merged["match_key"].notna()]
    out = merged.groupby("match_key").agg(
        griis_listed=("id", "count"),
        griis_is_invasive=("isInvasive", lambda s: "true" in set(str(x).lower() for x in s)),
    ).reset_index()
    out["griis_listed"] = True
    return out[["match_key", "griis_listed", "griis_is_invasive"]]


# Step 2e: VBA_FLORA100 (aggregated by SCI_NAME, same logic as I1's
# VBA25 handling)
def load_vba100(shp_path):
    sf = shapefile.Reader(shp_path)
    field_names = [f[0] for f in sf.fields[1:]]
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
        {"match_key": k, "vba100_record_count": v["count"], "vba100_most_recent_year": v["max_year"]}
        for k, v in agg.items()
    ]
    return pd.DataFrame(rows)


# Step 2f: ALA Monash occurrence records (geographic extent already
# confirmed to fall correctly within Monash)
def load_ala(csv_path):
    df = pd.read_csv(csv_path, dtype=str, low_memory=False)
    df["match_key"] = df["scientificName"].apply(normalize_name)
    df = df[df["match_key"].notna()]
    agg = df.groupby("match_key").agg(
        ala_record_count=("match_key", "count"),
        ala_most_recent_date=("eventDate", lambda s: max([x for x in s if isinstance(x, str) and x], default=None)),
    ).reset_index()
    return agg


# Step 3: Similarity-matching engine
# growth_form / woodiness / life_history must match exactly (compared as
#   sets, to handle multi-value strings).
# height is matched by range overlap.
# Alternatives are only searched for species whose recommendation is
#   Reconsider Planting or Use Caution.
# Candidate pool: species with recommendation in CANDIDATE_RECOMMENDATIONS
#   AND a complete set of traits.

CANDIDATE_RECOMMENDATIONS = {"Lower Concern", "Not Assessed"}


def tokenize(val):
    if val is None:
        return None
    return frozenset(str(val).split())


def height_overlap(a_min, a_max, b_min, b_max):
    if a_min is None or a_max is None or b_min is None or b_max is None:
        return False
    return a_min <= b_max and b_min <= a_max


def find_alternatives(merged_df, max_alternatives=3):
    candidates = merged_df[
        merged_df["recommendation"].isin(CANDIDATE_RECOMMENDATIONS)
        & merged_df["growth_form"].notna()
        & merged_df["woodiness"].notna()
        & merged_df["life_history"].notna()
        & merged_df["height_min"].notna()
        & merged_df["height_max"].notna()
    ].copy()

    alternatives_map = {}
    for _, row in merged_df.iterrows():
        if row["recommendation"] not in ("Reconsider Planting", "Use Caution"):
            continue
        if pd.isna(row["growth_form"]) or pd.isna(row["woodiness"]) or pd.isna(row["life_history"]) \
           or pd.isna(row["height_min"]) or pd.isna(row["height_max"]):
            alternatives_map[row["match_key"]] = {
                "status": "insufficient_trait_data",
                "alternatives": [],
            }
            continue

        gf = tokenize(row["growth_form"])
        wd = tokenize(row["woodiness"])
        lh = tokenize(row["life_history"])

        matches = candidates[
            (candidates["match_key"] != row["match_key"])
            & (candidates["growth_form"].apply(tokenize) == gf)
            & (candidates["woodiness"].apply(tokenize) == wd)
            & (candidates["life_history"].apply(tokenize) == lh)
            & candidates.apply(
                lambda c: height_overlap(row["height_min"], row["height_max"], c["height_min"], c["height_max"]),
                axis=1,
            )
        ].copy()

        # Family tie-breaker note: NOT applied in this version by team
        # decision -- matching stays on growth_form/woodiness/life_history/
        # height only. (A family-sort variant exists separately if the
        # team decides to use it later.)

        # Dedupe on match_key before taking the top N: subspecies/varieties
        # share a match_key and identical inherited trait data (see the
        # taxonomy-collapse note in normalize_name/load_vicflora), so
        # without this a single underlying species could fill 2+ of the
        # 3 alternative slots under different subspecies names.
        matches = matches.drop_duplicates(subset="match_key")

        alt_list = matches[["match_key", "scientific_name"]].head(max_alternatives).to_dict("records")
        alternatives_map[row["match_key"]] = {
            "status": "matched" if alt_list else "no_strict_match_found",
            "alternatives": alt_list,
        }
    return alternatives_map


# Main pipeline
def run_pipeline():
    if not Path(BASE).is_dir():
        raise FileNotFoundError(
            f"Data directory not found: {BASE}\n"
            f"Either create a 'data' folder next to this script containing the "
            f"input files, set PLANTASSURE_DATA_DIR, or pass the path as an "
            f"argument: python3 {Path(__file__).name} /path/to/data"
        )
    print(f"Using data directory: {BASE}")

    print("Step 2a: VicFlora ...")
    vf = load_vicflora(f"{BASE}/vicflora_monash_2026.csv")
    print(f"  -> {len(vf)} records")

    print("Step 2b: Advisory List ...")
    adv = load_advisory(f"{BASE}/Advisory-list-of-environmental-weeds-in-Victoria_2022.xlsx")
    print(f"  -> {len(adv)} records")

    print("Step 2c: AusTraits traits (pivoting long format to wide format) ...")
    traits = load_austraits_traits(f"{BASE}/austraits-7.0.0-flattened.parquet")
    print(f"  -> {len(traits)} species have trait data")

    print("Step 2d: GRIIS Australia ...")
    griis = load_griis(f"{BASE}/griis/dwca-griis-australia-v1")
    print(f"  -> {len(griis)} species found in GRIIS")

    print("Step 2e: VBA_FLORA100 ...")
    vba = load_vba100(f"{BASE}/order/ll_gda2020/esrishape/lga_polygon/MONASH-0/FLORAFAUNA1/VBA_FLORA100.shp")
    print(f"  -> {len(vba)} species")

    print("Step 2f: ALA Monash occurrence records ...")
    import zipfile
    with zipfile.ZipFile(f"{BASE}/records-2026-09-10.zip") as z:
        z.extract("records-2026-09-10.csv", "/tmp")
    ala = load_ala("/tmp/records-2026-09-10.csv")
    print(f"  -> {len(ala)} species")

    print("Step 3: Merging ...")
    merged = vf.merge(adv, on="match_key", how="left")
    merged = merged.merge(traits, on="match_key", how="left")
    merged = merged.merge(griis, on="match_key", how="left")
    merged = merged.merge(vba, on="match_key", how="left")
    merged = merged.merge(ala, on="match_key", how="left")

    print("Step 4: Handling missing values (conservative approach: flag as missing, no substitution guesses) ...")
    merged["Risk Rating"] = merged["Risk Rating"].fillna("Not Assessed / No exact match")
    merged["establishment_means"] = merged["establishment_means"].fillna("Not available")
    merged["degree_of_establishment"] = merged["degree_of_establishment"].fillna("Not available")
    merged["griis_listed"] = merged["griis_listed"].fillna(False)
    merged["griis_is_invasive"] = merged["griis_is_invasive"].fillna(False)

    for col in ["growth_form", "woodiness", "life_history"]:
        merged[col] = merged[col].where(merged[col].notna(), None)

    merged["vba100_most_recent_year"] = merged["vba100_most_recent_year"].astype(object)
    merged["ala_most_recent_date"] = merged["ala_most_recent_date"].astype(object)

    no_vba = merged["vba100_record_count"].isna()
    merged.loc[no_vba, "vba100_record_count"] = 0
    merged.loc[no_vba, "vba100_most_recent_year"] = "No local records found"
    merged.loc[~no_vba, "vba100_most_recent_year"] = merged.loc[~no_vba, "vba100_most_recent_year"].apply(
        lambda v: int(v) if pd.notna(v) else v
    )

    no_ala = merged["ala_record_count"].isna()
    merged.loc[no_ala, "ala_record_count"] = 0
    merged.loc[no_ala, "ala_most_recent_date"] = "No local records found"

    print("Step 5: Computing recommendation via rule engine ...")
    merged["recommendation"] = merged["Risk Rating"].apply(apply_rule)

    print("Step 6: Similarity matching (Find a Better Plant) ...")
    alt_map = find_alternatives(merged)

    print("Step 7: Building final output structure ...")
    output = []
    for _, row in merged.iterrows():
        mk = row["match_key"]
        is_introduced = str(row["establishment_means"]).strip().lower() == "introduced"
        griis_supplementary = None
        if is_introduced and row["griis_listed"]:
            griis_supplementary = {
                "listed_in_griis": True,
                "griis_flagged_invasive": bool(row["griis_is_invasive"]),
                "note": "Supplementary Australia-level evidence only; does not override Victorian DEECA classification.",
            }

        entry = {
            "scientific_name": row["scientific_name"],
            "common_name": row["vernacular_name"],
            "recommendation": row["recommendation"],
            "traits": {
                "growth_form": row["growth_form"],
                "woodiness": row["woodiness"],
                "life_history": row["life_history"],
                "height_min_m": row["height_min"] if pd.notna(row["height_min"]) else None,
                "height_max_m": row["height_max"] if pd.notna(row["height_max"]) else None,
            },
            "legal_status": {
                "available": False,
                "note": "No verified Victorian legal-status dataset available in this iteration; "
                        "filtering of legally regulated plants has NOT been applied. Known limitation.",
            },
            "griis_supplementary_evidence": griis_supplementary,
            "supporting_evidence": {
                "establishment_status": row["establishment_means"],
                "degree_of_establishment": row["degree_of_establishment"],
                "vba100_record_count": int(row["vba100_record_count"]),
                "vba100_most_recent_year": row["vba100_most_recent_year"],
                "ala_record_count": int(row["ala_record_count"]),
                "ala_most_recent_date": row["ala_most_recent_date"],
            },
            "alternatives": alt_map.get(mk, {"status": "not_applicable_lower_concern_or_not_assessed", "alternatives": []}),
            "data_sources": [
                "VicFlora", "2022 Advisory List (DEECA)", "AusTraits v7.0.0 (Wenk_2022/Wenk_2023)",
                "GRIIS Australia (supplementary)", "VBA Flora Records 100", "ALA Monash occurrence records",
            ],
        }
        output.append(entry)
    return output, merged


if __name__ == "__main__":
    result, merged_df = run_pipeline()

    # Output directory: same override pattern as BASE above -- an env
    # var takes priority, otherwise outputs land next to this script.
    OUTPUT_DIR = os.environ.get("PLANTASSURE_OUTPUT_DIR", str(SCRIPT_DIR))
    Path(OUTPUT_DIR).mkdir(parents=True, exist_ok=True)
    json_path = Path(OUTPUT_DIR) / "output_i2.json"
    csv_path = Path(OUTPUT_DIR) / "output_i2.csv"

    with open(json_path, "w", encoding="utf-8") as f:
        json.dump(result, f, ensure_ascii=False, indent=2, default=str)
    merged_df.to_csv(csv_path, index=False, encoding="utf-8-sig")

    print(f"\nOutputs written to: {OUTPUT_DIR}")
    print(f"Done! {len(result)} species processed in total")
    from collections import Counter as C
    print("Recommendation distribution:", C(r["recommendation"] for r in result))
    need_alt = [r for r in result if r["recommendation"] in ("Reconsider Planting", "Use Caution")]
    matched = [r for r in need_alt if r["alternatives"]["status"] == "matched"]
    no_match = [r for r in need_alt if r["alternatives"]["status"] == "no_strict_match_found"]
    insuf = [r for r in need_alt if r["alternatives"]["status"] == "insufficient_trait_data"]
    print(f"Species needing alternatives: {len(need_alt)}")
    print(f"  Successfully matched to an alternative: {len(matched)}")
    print(f"  Traits complete but no strict match found: {len(no_match)}")
    print(f"  Insufficient trait data to judge: {len(insuf)}")
