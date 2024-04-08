import sqlite3

conn = sqlite3.connect("../stm_info.db")
cursor = conn.cursor()
print("Connected to database stm_info.db")

cursor.execute("DROP TABLE IF EXISTS Routes;")
print("Dropped table routes")

sql = """CREATE TABLE Routes (
	--id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
	id INTEGER PRIMARY KEY NOT NULL,-- AUTOINCREMENT ,
	route_id INTEGER UNIQUE NOT NULL,
	route_long_name TEXT NOT NULL,
	route_type INTEGER NOT NULL,
	route_color TEXT NOT NULL
);"""
cursor.execute(sql)
print("Initialised table routes")

print("Inserting table and adding data")
with open("routes.txt", "r", encoding="utf-8") as file:
    file.readline()
    for line in file:
        tokens = line.replace("\n", "").replace("'", "''").split(",")
        sql = f"INSERT INTO Routes (route_id,route_long_name,route_type,route_color) VALUES (?,?,?,?);"
        cursor.execute(sql, (tokens[0],tokens[3],tokens[4],tokens[6]))
        conn.commit()
print("Successfully inserted table")

conn.close()
