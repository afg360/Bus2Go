import sqlite3

conn = sqlite3.connect("../stm_info.db", isolation_level=None)
conn.execute('PRAGMA encoding = "UTF-8"')
cursor = conn.cursor()
print("Connected to database stm_info.db")

# drop the table and index
query = "DROP TABLE IF EXISTS StopTimes;"
cursor.execute(query)
print("Dropped table StopTimes")
query = "DROP INDEX IF EXISTS StopTimesIndex;"
cursor.execute(query)
print("Dropped Index StopTimes\n")

# init the table
query = """CREATE TABLE StopTimes (
	--id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
	id INTEGER PRIMARY KEY NOT NULL,--AUTOINCREMENT,
	trip_id INTEGER NOT NULL REFERENCES Trips(trip_id),
	arrival_time TEXT NOT NULL,
	departure_time TEXT NOT NULL,
	stop_id INTEGER NOT NULL REFERENCES Stops(stop_id),
	stop_seq INTEGER NOT NULL
);
"""
cursor.execute(query)
print("Inserting table and adding data")

chunk_size = 1000000
with open("stop_times.txt", "r", encoding="utf-8") as file:
    file.readline()
    chunk = []
    cursor.execute("BEGIN TRANSACTION;")
    sql = "INSERT INTO StopTimes (trip_id,arrival_time,departure_time,stop_id,stop_seq) VALUES (?,?,?,?,?);\n"
    i = 1
    for line in file:
        chunk.append(line.split(","))
        if len(chunk) >= chunk_size:
            print(f"Created chunk #{i}, executing query")
            cursor.executemany(sql, chunk)
            conn.commit()
            i += 1
            chunk = []
            cursor.execute("BEGIN TRANSACTION;")
    if chunk is not None:
        print("Executing final query")
        cursor.executemany(sql, chunk)
        conn.commit()
print("Successfully inserted table")

#no need bcz trips already provides it
#query = "CREATE INDEX StopTimesIndex ON StopTimes(tripid);"

#need that bcz bottleneck
query = "CREATE INDEX StopTimesIndex ON StopTimes(stop_id,trip_id);"
print("Creating index for StopTimes on stopid and tripid")
cursor.execute(query)
print("Successfully created index for table StopTimes")
conn.close()
