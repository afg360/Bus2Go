import requests
import threading
import zipfile
import os
import sqlite3


def download(url : str, destination : str) -> None:
    """download and create respective directories"""
    zip_file = f"{destination}.zip"
    response = requests.get(url)
    if response.status_code == 200:
        with open(zip_file, "wb") as file:
            file.write(response.content)
        print(f"Downloaded {url} to {zip_file} successfully")
        if not os.path.exists(f"./{destination}"):
            os.makedirs(destination)
        with zipfile.ZipFile(zip_file, "r") as zip:
            zip.extractall(f"./{destination}")
        print(f"Extracted file from {zip_file}")
        os.remove(zip_file)
        print("Removed zip file")
    else:
        print(f"Failed to download {url}")


def db_data_init(conn, agency : str) -> None:
    """Initialise the data in the database associated to that agency"""
    calendar_table(conn, agency)
    route_table(conn, agency)
    forms_table(conn, agency)
    shapes_table(conn, agency)
    stop_times_table(conn, agency)
    stops_table(conn, agency)
    trips_table(conn, agency)


def calendar_table(conn, agency) -> None:
    cursor = conn.cursor()
    print("Connected to database exo_info.db")
    sql = """CREATE TABLE IF NOT EXISTS Calendar (
    	id INTEGER PRIMARY KEY NOT NULL,
    	service_id TEXT UNIQUE NOT NULL,
        days TEXT NOT NULL,
    	start_date INTEGER NOT NULL,
    	end_date INTEGER NOT NULL
    );"""
    cursor.execute(sql)
    print("Initialised table Calendar")
    print("Inserting table and adding data")
    with open(f"./{agency}/calendar.txt", "r", encoding="utf-8") as file:
        file.readline()
        for line in file:
            tokens = line.replace("\n", "").replace("'", "''").split(",")
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


def forms_table(conn, agency):
    """Custom table to put together all unique shape_ids"""
    cursor = conn.cursor()

    sql = """CREATE TABLE IF NOT EXISTS Forms (
    	id INTEGER PRIMARY KEY NOT NULL,
    	shape_id TEXT UNIQUE NOT NULL
    );"""
    cursor.execute(sql)
    print("Initialised table Forms")

    print("Inserting tables")
    queries = []
    with open(f"./{agency}/shapes.txt", "r", encoding="utf-8") as file:
        file.readline()
        prev = ""
        for line in file:
            tokens = line.split(",")
            shape_id = tokens[0]
            if not shape_id == prev:
                queries.append((f"{agency}-{tokens[0]}",))
                prev = shape_id
        sql = "INSERT INTO Forms (shape_id) VALUES (?);\n"
        cursor.execute("BEGIN TRANSACTION;")
        cursor.executemany(sql, queries)
        conn.commit()
    print("Successfully inserted table")


def route_table(conn, agency):
    cursor = conn.cursor()
    print("Connected to database exo_info.db")

    sql = """CREATE TABLE IF NOT EXISTS Routes (
    	id INTEGER PRIMARY KEY NOT NULL,
    	route_id TEXT UNIQUE NOT NULL,
    	route_long_name TEXT NOT NULL,
    	route_type INTEGER NOT NULL,
    	route_color TEXT NOT NULL,
    	route_text_color TEXT NOT NULL
    );"""
    cursor.execute(sql)
    print("Initialised table Routes")

    print("Inserting table and adding data")
    with open(f"./{agency}/routes.txt", "r", encoding="utf-8") as file:
        file.readline()
        for line in file:
            tokens = line.replace("\n", "").replace("'", "''").split(",")
            sql = """INSERT INTO Routes (route_id,route_long_name,route_type,route_color,route_text_color)
            VALUES (?,?,?,?,?);"""
            cursor.execute(sql, (f"{agency}-{tokens[0]}",tokens[3],tokens[4],tokens[5], tokens[6]))
            conn.commit()
    print("Successfully inserted Routes table")
    cursor.close()


def shapes_table(conn, agency):
    cursor = conn.cursor()
    print("Connected to database exo_info.db")

    sql = """CREATE TABLE IF NOT EXISTS Shapes (
    	id INTEGER PRIMARY KEY NOT NULL,
    	shape_id TEXT NOT NULL REFERENCES Forms(shape_id),
    	lat REAL NOT NULL,
    	long REAL NOT NULL,
    	sequence INTEGER NOT NULL
    );"""
    cursor.execute(sql)
    print("Initialised table shapes")

    print("Inserting tables")
    queries = []
    with open(f"./{agency}/shapes.txt", "r", encoding="utf-8") as file:
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


def stop_times_table(conn, agency):
    cursor = conn.cursor()
    print("Connected to database exo_info.db")

    #COULD INCLUDE TIMEPOINT: 0 = approx, 1 = exact
    query = """CREATE TABLE IF NOT EXISTS StopTimes (
    	id INTEGER PRIMARY KEY NOT NULL,
    	trip_id TEXT NOT NULL REFERENCES Trips(trip_id),
    	arrival_time TEXT NOT NULL,
    	departure_time TEXT NOT NULL,
    	stop_id TEXT NOT NULL REFERENCES Stops(stop_id),
    	stop_seq INTEGER NOT NULL
    );
    """
    cursor.execute(query)
    print("Inserting table and adding data")

    chunk_size = 1000000
    with open(f"./{agency}/stop_times.txt", "r", encoding="utf-8") as file:
        file.readline()
        chunk = []
        cursor.execute("BEGIN TRANSACTION;")
        sql = "INSERT INTO StopTimes (trip_id,arrival_time,departure_time,stop_id,stop_seq) VALUES (?,?,?,?,?);\n"
        i = 1
        for line in file:
            list = line.split(",")
            chunk.append((list[0],list[1],list[2],f"{agency}-{list[3]}",list[4]))
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


    cursor.close()


def stops_table(conn, agency):
    cursor = conn.cursor()
    print("Connected to database exo_info.db")

    sql = """CREATE TABLE IF NOT EXISTS Stops (
    	id INTEGER PRIMARY KEY NOT NULL,
    	stop_id TEXT UNIQUE NOT NULL,
    	stop_name TEXT NOT NULL,
    	lat REAL NOT NULL,
    	long REAL NOT NULL,
    	stop_code TEXT NOT NULL,
    	wheelchair INTEGER NOT NULL
    );"""
    cursor.execute(sql)
    print("Initialised table stops")

    print("Inserting tables")
    chunk = []
    with open(f"./{agency}/stops.txt", "r", encoding="utf-8") as file:
        file.readline()
        for line in file:
            tokens = line.split(",")
            chunk.append((f"{agency}-{tokens[0]}",tokens[1],tokens[2], tokens[3], tokens[5], tokens[6]))
        sql = "INSERT INTO Stops (stop_id,stop_name,lat,long,stop_code,wheelchair) VALUES (?,?,?,?,?,?);\n"

        cursor.execute("BEGIN TRANSACTION;")
        cursor.executemany(sql, chunk)
        conn.commit()
    print("Successfully inserted table")
    cursor.close()


def trips_table(conn, agency):
    cursor = conn.cursor()
    print("Connected to database exo_info.db")

    query = """CREATE TABLE IF NOT EXISTS Trips (
    	id INTEGER PRIMARY KEY NOT NULL,
    	trip_id TEXT NOT NULL,
    	route_id TEXT NOT NULL REFERENCES Routes(route_id),
    	service_id TEXT NOT NULL REFERENCES Calendar(service_id),
    	trip_headsign TEXT NOT NULL,
    	direction_id INTEGER NOT NULL,
    	shape_id TEXT NOT NULL REFERENCES Forms(shape_id),
    	wheelchair INTEGER NOT NULL
    );"""
    cursor.execute(query)

    print("Inserting table and adding data")
    chunk_size = 500000
    with open(f"./{agency}/trips.txt", "r", encoding="utf-8") as file:
        file.readline()
        chunk = []
        i = 1
        for line in file:
            tokens = line.split(",")
            chunk.append((tokens[2],f"{agency}-{tokens[0]}",tokens[1],tokens[3],tokens[4],tokens[5],tokens[6]))
            sql = "INSERT INTO Trips (trip_id,route_id,service_id,trip_headsign,direction_id,shape_id,wheelchair) VALUES (?,?,?,?,?,?,?);\n"
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


def main():
    files = [
            "citcrc",   #autos Chambly-Richelieu-Carignan
            "cithsl",   #autos Haut-Saint-Laurent
            "citla",    #autos Laurentides
            "citpi",    #autos La Presqu'île
            "citlr",    #autos Le Richelain
            "citrous",  #autos Roussillon
            "citsv",    #autos Sorel-Varennes
            "citso",    #autos Sud-ouest
            "citvr",    #autos Vallée du Richelieu
            "mrclasso", #autos L'Assomption
            "mrclm",    #autos Terrebonne-Mascouche
            "trains",
            "omitsju",  #autos Sainte-Julie
            "lrrs"      #autos Le Richelain et Roussillon
    ]
    jobs = []

    import sys
    if (len(sys.argv) > 1 and sys.argv[1] == "no-download"):
        pass
    else:
        for file in files:
            thread = threading.Thread(target=download, args=(
                "https://exo.quebec/xdata/" + file + "/google_transit.zip", file
            ))
            jobs.append(thread)
            thread.start()
        for job in jobs:
            job.join()

    conn = sqlite3.connect("./exo_info.db")
    for dir in files:
        print(f"Initialising data for {dir}")
        db_data_init(conn, dir)

    cursor = conn.cursor()
    print("Creating index for StopTimes on stopid and tripid")
    cursor.execute("CREATE INDEX StopTimesIndexStopId ON StopTimes(stop_id);")
    cursor.execute("CREATE INDEX StopTimesIndexTripId ON StopTimes(trip_id);")
    cursor.execute("CREATE INDEX TripsIndexTripId ON Trips(trip_id);")
    print("Successfully created index for table StopTimes")
    conn.close()

if __name__ == "__main__":
    main()