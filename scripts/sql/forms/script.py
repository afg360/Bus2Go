
import sqlite3

conn = sqlite3.connect("../stm_info.db", isolation_level=None)
conn.execute('PRAGMA encoding = "UTF-8"')
cursor = conn.cursor()
print("Connected to database stm_info.db")

cursor.execute("DROP TABLE IF EXISTS Forms;")
print("Dropped table forms")

sql = """CREATE TABLE Forms(
	id INTEGER PRIMARY KEY NOT NULL,--AUTOINCREMENT,
	shape_id INTEGER UNIQUE NOT NULL --perhaps could add more data
);"""
cursor.execute(sql)
print("Initialised table Forms")

print("Inserting tables")
queries = []
with open("../shapes/shapes.txt", "r", encoding="utf-8") as file:
    file.readline()
    prev = ""
    for line in file:
        tokens = line.split(",")
        shape_id = tokens[0]
        if not shape_id == prev:
            queries.append((tokens[0],))
            prev = shape_id
    sql = "INSERT INTO Forms (shape_id) VALUES (?);\n"
    cursor.execute("BEGIN TRANSACTION;")
    cursor.executemany(sql, queries)
    conn.commit()
print("Successfully inserted table")

conn.close()
