import sqlite3

db_path = r"c:\Databaseofdiseasebackend\db.sqlite3"
conn = sqlite3.connect(db_path)
cursor = conn.cursor()

cursor.execute("SELECT name FROM sqlite_master WHERE type='table'")
tables = cursor.fetchall()
print("Tables:", tables)

for t in tables:
    if 'patientdisease' in t[0].lower():
        cursor.execute(f"PRAGMA table_info({t[0]})")
        print(f"Columns for {t[0]}:", cursor.fetchall())
        cursor.execute(f"SELECT * FROM {t[0]} LIMIT 2")
        print("Rows:", cursor.fetchall())
