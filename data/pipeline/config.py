import os
 
try:
    from dotenv import load_dotenv
    load_dotenv()
except ImportError:
    pass  # python-dotenv not installed; fall back to real environment variables
 
DB_CONFIG = {
    "host": os.environ.get("DB_HOST", "localhost"),
    "port": int(os.environ.get("DB_PORT", "3306")),
    "user": os.environ.get("DB_USER", "root"),
    "password": os.environ.get("DB_PASSWORD"),
    "database": os.environ.get("DB_NAME", "test_db"),
}
 
if not DB_CONFIG["password"]:
    print(
        "Warning: DB_PASSWORD is not set. Species data will still be written "
        "to output.json/output.csv, but the MySQL write step will fail. "
        "Copy .env.example to .env and fill in your local MySQL password "
        "if you need the database write to succeed."
    )
 