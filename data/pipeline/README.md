# FIT5120 Project — Check Before You Plant

## Setup

1. Copy `.env.example` to `.env` and fill in your local MySQL password:
   ```
   cp .env.example .env
   ```
2. Install dependencies: `pip install -r requirements.txt` (add one if we don't have it yet — at minimum: fastapi, uvicorn, pandas, sqlalchemy, pymysql, python-dotenv).
3. Place the source data files below into a local `data/` folder (this folder is git-ignored — it's too large to commit).

## Data (not tracked in git — download separately)

- `data/iNaturalist.csv` — iNaturalist occurrence export (~390MB)
- `data/vicflora_monash_2026.csv` — VicFlora / VBA occurrence data for the Monash LGA
- `data/Advisory-list-of-environmental-weeds-in-Victoria_2022.xlsx` — Agriculture Victoria's official weed advisory list
- `data/Order_ELFIES/` — Monash LGA boundary shapefiles

Ask whoever ran the original data pull for a copy, or re-download from the original sources (ALA/iNaturalist, VBA/VicFlora, Agriculture Victoria) if you need a fresh export.

## Running the pipeline

```
python pipeline.py
```

This produces `output.json` and `output.csv`.

## Running the API

```
uvicorn main:app --reload
```

## Security note

Never commit real database credentials. `config.py` reads them from environment variables (via `.env`, which is git-ignored) — don't hardcode values back into it.
