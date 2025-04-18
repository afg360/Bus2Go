#this script is to create a table with more info than stoptimes
import requests
import sys
import zipfile
import os
import sqlite3
import datetime

def convert_str_to_date(date: str) -> datetime.datetime:
    """Convert the time string to a datetime"""
    return datetime.datetime(int(date[0:4]), int(date[4:6]), int(date[6:8]))

def download(url : str, path: str): #destination : str) -> None:
    """download and create respective directories"""
    zip_file = f"{path}/data.zip"   #f"{destination}.zip"
    print(f"Downloading from {url}")
    try:
        response = requests.get(url)
        if response.status_code == 200:
            with open(zip_file, "wb") as file:
                file.write(response.content)
            print(f"Downloaded {url} to {zip_file} successfully")
            #if not os.path.exists(f"./{destination}"):
            #    os.makedirs(destination)
            with zipfile.ZipFile(zip_file, "r") as zip:
                zip.extractall(f"{path}")#{destination}")
            print(f"Extracted file from {zip_file}")
            os.remove(zip_file)
            print("Removed zip file")
        else:
            print(f"Failed to download {url}")
    except requests.ConnectionError:
        print("You are not connected to the internet, aborting the script")
        exit(1)


def db_data_init(conn, path: str) -> None:
    """Initialise the data in the database associated to that agency"""
    calendar_table(conn, path)
    calendar_dates_table(conn, path)
    route_table(conn, path)
    forms_table(conn, path)
    shapes_table(conn, path)
    stop_times_table(conn, path)
    stops_table(conn, path)
    trips_table(conn, path)
    stops_info_table(conn)


def calendar_table(conn, path: str):
    cursor = conn.cursor()
    cursor.execute("DROP TABLE IF EXISTS Calendar;")
    print("Dropped table Calendar")

    sql = """CREATE TABLE Calendar (
    	--id INTEGER PRIMARY KEY NOT NULL,
    	service_id TEXT PRIMARY KEY NOT NULL,
        days TEXT NOT NULL,
    	start_date INTEGER NOT NULL,
    	end_date INTEGER NOT NULL
    );"""
    cursor.execute(sql)
    print("Initialised table Calendar")

    print("Inserting table and adding data")
    with open(f"{path}/calendar.txt", "r", encoding="utf-8") as file:
        file.readline()
        #we will be skipping calendar dates that are beyond today's date...
        #TEST, MIGHT BREAK STUFF
        today = datetime.datetime.today()
        for line in file:
            tokens = line.replace("\n", "").replace("'", "''").split(",")
            date = convert_str_to_date(tokens[9])
            #date = Date(tokens[9])
            #for now, skip calendars where start_date == end_date
            #also, we skip dates that have passed the current date from when the script is being run
            #seems to have a bug here...?
            #if date == convert_str_to_date(tokens[8]):
            #    continue
            #if date.year < today.year:
            #    continue
            #elif date.year == today.year and date.month < today.month:
            #    continue
            #elif date.year == today.today and date.month == today.month and date.day < today.day:
            #    continue
            #check all the possible letters
            days = ""
            if tokens[1] == "1":
                days += "m"
            if tokens[2] == "1":
                days += "t"
            if tokens[3] == "1":
                days += "w"
            if tokens[4] == "1":
                days += "y"
            if tokens[5] == "1":
                days += "f"
            if tokens[6] == "1":
                days += "s"
            if tokens[7] == "1":
                days += "d"
            sql = f"INSERT INTO Calendar (service_id,days,start_date,end_date) VALUES (?,?,?,?);"
            cursor.execute(sql, (tokens[0], days, tokens[8], tokens[9]))
            conn.commit()
    print("Successfully inserted table")

    cursor.close()

def calendar_dates_table(conn, path: str):
    cursor = conn.cursor()
    cursor.execute("DROP TABLE IF EXISTS CalendarDates;")
    print("Dropped table CalendarDates")

    sql = """CREATE TABLE CalendarDates (
    	service_id TEXT REFERENCES Calendar(service_id) NOT NULL,
        date TEXT NOT NULL,
        exception_type INTEGER NOT NULL,
        PRIMARY KEY (service_id, date)
    );"""
    cursor.execute(sql)
    print("Initialised table CalendarDates")

    print("Inserting table and adding data")
    with open(f"{path}/calendar_dates.txt", "r", encoding="utf-8") as file:
        file.readline()
        #try to add a row. if it doesn't exist in calendar, 
        #simply skip (may be because we deleted the 
        #original thing because past the date)
        for line in file:
            tokens = line.replace("\n", "").replace("'", "''").split(",")
            sql = f"INSERT INTO CalendarDates (service_id,date,exception_type) VALUES (?,?,?);"
            cursor.execute(sql, (tokens[0], tokens[1], tokens[2]))
            conn.commit()

    print("Successfully inserted table")
    cursor.close()


def forms_table(conn, path: str):
    cursor = conn.cursor()
    cursor.execute("DROP TABLE IF EXISTS Forms;")
    print("Dropped table forms")

    sql = """CREATE TABLE Forms(
    	id INTEGER PRIMARY KEY NOT NULL,
    	shape_id INTEGER UNIQUE NOT NULL --perhaps could add more data
    );"""
    cursor.execute(sql)
    print("Initialised table Forms")

    print("Inserting tables")
    queries = []
    with open(f"{path}/shapes.txt", "r", encoding="utf-8") as file:
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

    cursor.close()


def route_table(conn, path: str):
    cursor = conn.cursor()
    cursor.execute("DROP TABLE IF EXISTS Routes;")
    print("Dropped table routes")

    sql = """CREATE TABLE Routes (
    	id INTEGER PRIMARY KEY NOT NULL,
    	route_id INTEGER UNIQUE NOT NULL,
    	route_long_name TEXT NOT NULL,
    	route_type INTEGER NOT NULL,
    	route_color TEXT NOT NULL
    );"""
    cursor.execute(sql)
    print("Initialised table routes")

    print("Inserting table and adding data")
    with open(f"{path}/routes.txt", "r", encoding="utf-8") as file:
        file.readline()
        for line in file:
            tokens = line.replace("\n", "").replace("'", "''").split(",")
            sql = f"INSERT INTO Routes (route_id,route_long_name,route_type,route_color) VALUES (?,?,?,?);"
            cursor.execute(sql, (tokens[0],tokens[3],tokens[4],tokens[6]))
            conn.commit()
    print("Successfully inserted table")

    cursor.close()


def shapes_table(conn, path: str):
    cursor = conn.cursor()
    cursor.execute("DROP TABLE IF EXISTS Shapes;")
    print("Dropped table shapes")

    sql = """CREATE TABLE Shapes(
    	id INTEGER PRIMARY KEY NOT NULL,
    	shape_id INTEGER NOT NULL REFERENCES Forms(shape_id),
    	lat REAL NOT NULL,
    	long REAL NOT NULL,
    	sequence INTEGER NOT NULL
    );"""
    cursor.execute(sql)
    print("Initialised table shapes")

    print("Inserting tables")
    queries = []
    with open(f"{path}/shapes.txt", "r", encoding="utf-8") as file:
        file.readline()
        for line in file:
            tokens = line.split(",")
            queries.append((tokens[0], tokens[1], tokens[2], tokens[3]))
        sql = "INSERT INTO Shapes (shape_id,lat,long,sequence) VALUES (?,?,?,?);\n"
        cursor.execute("BEGIN TRANSACTION;")
        cursor.executemany(sql, queries)
        conn.commit()
    print("Successfully inserted table")

    cursor.close()


def stop_times_table(conn, path):
    cursor = conn.cursor()
    # drop the table and index
    query = "DROP TABLE IF EXISTS StopTimes;"
    cursor.execute(query)
    print("Dropped table StopTimes")
    query = "DROP INDEX IF EXISTS StopTimesIndex;"
    cursor.execute(query)
    print("Dropped Index StopTimes\n")

    # init the table
    query = """CREATE TABLE StopTimes (
    	id INTEGER PRIMARY KEY NOT NULL,
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
    with open(f"{path}/stop_times.txt", "r", encoding="utf-8") as file:
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
    cursor.close()


def stops_table(conn, path):
    cursor = conn.cursor()

    sql = "DROP TABLE IF EXISTS Stops;"
    cursor.execute(sql)
    print("Dropped table Stops")

    sql = """CREATE TABLE Stops (
    	id INTEGER PRIMARY KEY NOT NULL,
    	stop_id TEXT UNIQUE NOT NULL,
    	stop_code INTEGER NOT NULL,
    	stop_name TEXT NOT NULL,
    	lat REAL NOT NULL,
    	long REAL NOT NULL,
    	wheelchair INTEGER NOT NULL
    );"""
    cursor.execute(sql)
    print("Initialised table stops")

    print("Inserting tables")
    chunk = []
    with open(f"{path}/stops.txt", "r", encoding="utf-8") as file:
        file.readline()
        for line in file:
            tokens = line.split(",")
            chunk.append((tokens[0], tokens[1], tokens[2], tokens[3], tokens[4], tokens[8]))
        sql = "INSERT INTO Stops (stop_id,stop_code,stop_name,lat,long,wheelchair) VALUES (?,?,?,?,?,?);\n"

        cursor.execute("BEGIN TRANSACTION;")
        cursor.executemany(sql, chunk)
        conn.commit()
    print("Successfully inserted table")

    cursor.close()


def trips_table(conn, path: str):
    cursor = conn.cursor()

    query = "DROP TABLE IF EXISTS Trips;"
    cursor.execute(query)
    print("Dropped table Trips")
    query = "DROP INDEX IF EXISTS TripsIndex;"
    cursor.execute(query)
    print("Dropped Index Trips\n")

    # init the table
    query = """CREATE TABLE Trips (
    	id INTEGER PRIMARY KEY NOT NULL,
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
    with open(f"{path}/trips.txt", "r", encoding="utf-8") as file:
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
    cursor.close()


def stops_info_table(conn):
        cursor = conn.cursor()

        print("Dropping table StopsInfo")
        cursor.execute("DROP TABLE IF EXISTS StopsInfo")
        print("Dropping index stopsinfo")
        cursor.execute("DROP INDEX IF EXISTS StopsInfoIndex")

        #save the calendar.service_id instead....?
        create = """CREATE TABLE IF NOT EXISTS StopsInfo(
        id INTEGER PRIMARY KEY NOT NULL,
        stop_name TEXT NOT NULL,
        route_id INTEGER NOT NULL,
        trip_headsign TEXT NOT NULL,
        --days TEXT NOT NULL,
        service_id TEXT NOT NULL REFERENCES Calendar(service_id),
        arrival_time TEXT NOT NULL,
        stop_seq INTEGER NOT NULL
        );
        """
        print("Creating table StopsInfo")
        cursor.execute(create)

        #sql = """INSERT INTO StopsInfo(stop_name,route_id,trip_headsign,days,arrival_time,stop_seq)
        sql = """INSERT INTO StopsInfo(stop_name,route_id,trip_headsign,service_id,arrival_time,stop_seq)
        SELECT stops.stop_name,trips.route_id,trips.trip_headsign,calendar.service_id,arrival_time,stoptimes.stop_seq
        --SELECT stops.stop_name,trips.route_id,trips.trip_headsign,calendar.days,arrival_time,stoptimes.stop_seq
        FROM stoptimes JOIN trips ON stoptimes.trip_id = trips.trip_id
        JOIN calendar ON calendar.service_id = trips.service_id
        JOIN stops ON stoptimes.stop_id = stops.stop_code;
        """
        print("Inserting data into StopsInfo")
        cursor.execute(sql)
        print("Dropping table and index StopTimes")
        cursor.execute("DROP TABLE IF EXISTS StopTimes;")
        cursor.execute("DROP INDEX IF EXISTS StopTimesIndex;")
        conn.commit()
        print("Vacuuming database")
        cursor.execute("VACUUM;")

        print("Creating index on StopsInfo")
        cursor.execute("CREATE INDEX StopsInfoIndex ON StopsInfo(route_id,stop_name)")

        cursor.close()


def main():
    path = "./stm"
    if not (len(sys.argv) > 1 and sys.argv[1] == "no-download"):
        download("https://www.stm.info/sites/default/files/gtfs/gtfs_stm.zip", path)

    conn = sqlite3.connect(f"{path}/stm_info.db")
    conn.execute('PRAGMA encoding = "UTF-8"')
    db_data_init(conn, path)
    conn.close()


if __name__ == "__main__":
    main()
