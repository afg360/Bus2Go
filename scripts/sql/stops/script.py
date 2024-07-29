import sqlite3

conn = sqlite3.connect("../stm_info.db", isolation_level=None)
conn.execute('PRAGMA encoding = "UTF-8"')
cursor = conn.cursor()
print("Connected to database stm_info.db")

sql = "DROP TABLE IF EXISTS Stops;"
cursor.execute(sql)
print("Dropped table Stops")

sql = """CREATE TABLE Stops (
	--id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
	id INTEGER PRIMARY KEY NOT NULL,--AUTOINCREMENT,
	stop_id TEXT UNIQUE NOT NULL,
	stop_code INTEGER NOT NULL,
	stop_name TEXT NOT NULL,
	lat REAL NOT NULL,
	long REAL NOT NULL,
	wheelchair INTEGER NOT NULL
	--parent_station
);"""
cursor.execute(sql)
print("Initialised table stops")

print("Inserting tables")
chunk = []
with open("stops.txt", "r", encoding="utf-8") as file:
    file.readline()
    for line in file:
        tokens = line.split(",")
        chunk.append((tokens[0], tokens[1], tokens[2], tokens[3], tokens[4], tokens[8]))
    sql = "INSERT INTO Stops (stop_id,stop_code,stop_name,lat,long,wheelchair) VALUES (?,?,?,?,?,?);\n"
        
    cursor.execute("BEGIN TRANSACTION;")
    cursor.executemany(sql, chunk)
    conn.commit()
print("Successfully inserted table")

conn.close()
