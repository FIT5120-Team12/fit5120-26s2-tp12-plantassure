from fastapi import FastAPI
from fastapi.responses import StreamingResponse
import pandas as pd
from sqlalchemy import create_engine
import io

from config import DB_CONFIG

app = FastAPI(title="数据清洗API")

engine = create_engine(
    f"mysql+pymysql://{DB_CONFIG['user']}:{DB_CONFIG['password']}"
    f"@{DB_CONFIG['host']}:{DB_CONFIG['port']}/{DB_CONFIG['database']}"
)

def get_cleaned_df():
    return pd.read_sql("SELECT * FROM species_data", con=engine)

@app.get("/api/clean-data")
def clean_data_json():
    df = get_cleaned_df()
    return df.to_dict(orient="records")

@app.get("/api/clean-data.csv")
def clean_data_csv():
    df = get_cleaned_df()
    stream = io.StringIO()
    df.to_csv(stream, index=False, encoding="utf-8-sig")
    return StreamingResponse(
        iter([stream.getvalue()]),
        media_type="text/csv",
        headers={"Content-Disposition": "attachment; filename=cleaned_data.csv"}
    )

@app.get("/health")
def health():
    return {"status": "ok"}