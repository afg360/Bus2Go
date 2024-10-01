#this script is to create a table with more info than stoptimes

import sqlite3

connection = sqlite3.connect("sql/stm_info.db")
cur = connection.cursor()

print("Dropping table StopsInfo")
cur.execute("DROP TABLE IF EXISTS StopsInfo")
print("Dropping index stopsinfo")
cur.execute("DROP INDEX IF EXISTS StopsInfoIndex")

create = """CREATE TABLE IF NOT EXISTS StopsInfo(
id INTEGER PRIMARY KEY NOT NULL,
stop_name TEXT NOT NULL,
trip_headsign TEXT NOT NULL,
days TEXT NOT NULL,
arrival_time TEXT NOT NULL,
stop_seq INTEGER NOT NULL
);
"""
print("Creating table StopsInfo")
cur.execute(create)

sql = """INSERT INTO StopsInfo(stop_name,trip_headsign,days,arrival_time,stop_seq)
SELECT stops.stop_name,trips.trip_headsign,calendar.days,arrival_time,stoptimes.stop_seq 
FROM stoptimes JOIN trips ON stoptimes.trip_id = trips.trip_id 
JOIN calendar ON calendar.service_id = trips.service_id 
JOIN stops ON stoptimes.stop_id = stops.stop_id;
"""
print("Inserting data into StopsInfo")
cur.execute(sql)
print("Dropping table and index StopTimes")
cur.execute("DROP TABLE IF EXISTS StopTimes;")
cur.execute("DROP INDEX IF EXISTS StopTimesIndex;")
connection.commit()
print("Vacuuming database")
cur.execute("VACUUM;")

print("Creating index on StopsInfo")
cur.execute("CREATE INDEX StopsInfoIndex ON StopsInfo(trip_headsign,stop_name)")
