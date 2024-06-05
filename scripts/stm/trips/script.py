import sqlite3

conn = sqlite3.connect("../stm_info.db")
cursor = conn.cursor()
print("Connected to database stm_info.db")

query = "DROP TABLE IF EXISTS Trips;"
cursor.execute(query)
print("Dropped table Trips")
query = "DROP INDEX IF EXISTS TripsIndex;"
cursor.execute(query)
print("Dropped Index Trips\n")

# init the table
query = """CREATE TABLE Trips (
	id INTEGER PRIMARY KEY NOT NULL,--AUTOINCREMENT,
	trip_id INTEGER NOT NULL,
	route_id INTEGER NOT NULL REFERENCES Routes(route_id),
	service_id TEXT NOT NULL REFERENCES Calendar(service_id),
	trip_headsign TEXT NOT NULL,
	direction_id INTEGER NOT NULL,
	shape_id INTEGER NOT NULL REFERENCES Forms(shape_id),
	wheelchair_accessible INTEGER NOT NULL
);"""
cursor.execute(query)

print("Inserting table and adding data")
chunk_size = 500000
with open("trips.txt", "r", encoding="utf-8") as file:
    file.readline()
    chunk = []
    i = 1
    for line in file:
        tokens = line.split(",")
        chunk.append((tokens[2],tokens[0],tokens[1],tokens[3],tokens[4],tokens[5],tokens[6]))
        sql = "INSERT INTO Trips (trip_id,route_id,service_id,trip_headsign,direction_id,shape_id,wheelchair_accessible) VALUES (?,?,?,?,?,?,?);\n"
        if len(chunk) >= chunk_size:
            print(f"Created chunk #{i}. Executing query")
            cursor.executemany(sql, chunk)
            conn.commit()
            chunk = []
    if not chunk is None:
        print("Executing final query")
        cursor.executemany(sql, chunk)
        conn.commit()
print("Successfully inserted table")

#query = "CREATE INDEX TripsIndex ON Trips(tripid);"
#print("Creating index for Trips")
#cursor.execute(query)
#print("Successfully created index for table Trips")
conn.close()
