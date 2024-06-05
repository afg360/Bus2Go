import sqlite3

conn = sqlite3.connect("../stm_info.db")
cursor = conn.cursor()
print("Connected to database stm_info.db")

cursor.execute("DROP TABLE IF EXISTS Calendar;")
print("Dropped table Calendar")

sql = """CREATE TABLE Calendar (
	--id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
	id INTEGER PRIMARY KEY NOT NULL,-- AUTOINCREMENT ,
	service_id TEXT UNIQUE NOT NULL,
    days TEXT NOT NULL, --could add a constraint
	--m INTEGER NOT NULL, --Monday
	--t INTEGER NOT NULL, --Tuesday
	--w INTEGER NOT NULL, --Wednesday
	--y INTEGER NOT NULL, --Thursday
	--f INTEGER NOT NULL, --Friday
	--s INTEGER NOT NULL, --Saturday
	--d INTEGER NOT NULL, --Sunday
	start_date INTEGER NOT NULL,
	end_date INTEGER NOT NULL
);"""
cursor.execute(sql)
print("Initialised table Calendar")

print("Inserting table and adding data")
with open("calendar.txt", "r", encoding="utf-8") as file:
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

conn.close()
