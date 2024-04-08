import sqlite3

conn = sqlite3.connect("../stm_info.db", isolation_level=None)
conn.execute('PRAGMA encoding = "UTF-8"')
cursor = conn.cursor()
print("Connected to database stm_info.db")

cursor.execute("DROP TABLE IF EXISTS Shapes;")
print("Dropped table shapes")

sql = """CREATE TABLE Shapes(
	id INTEGER PRIMARY KEY NOT NULL,--AUTOINCREMENT,
	shape_id INTEGER NOT NULL REFERENCES Forms(shape_id),
	lat REAL NOT NULL,
	long REAL NOT NULL,
	sequence INTEGER NOT NULL
);"""
cursor.execute(sql)
print("Initialised table shapes")

print("Inserting tables")
queries = []
with open("shapes.txt", "r", encoding="utf-8") as file:
    file.readline()
    for line in file:
        tokens = line.split(",")
        queries.append((tokens[0], tokens[1], tokens[2], tokens[3]))
    sql = "INSERT INTO Shapes (shape_id,lat,long,sequence) VALUES (?,?,?,?);\n"
    cursor.execute("BEGIN TRANSACTION;")
    cursor.executemany(sql, queries)
    conn.commit()
print("Successfully inserted table")

conn.close()
