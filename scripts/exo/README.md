# Python script
The python script in this directory downloads the data static data from the exo website, and initialises the database with the 
appropriate data.
**This schema is not 100% similar to the schema for the stm data**

Database Schema for each agency:

```json
[
  {
    "table": "Calendar",
    "schema": {
      "id": "INTEGER PRIMARY KEY",
      "service_id": "TEXT UNIQUE NOT NULL",
      "days": "TEXT NOT NULL",
      "start_date": "INTEGER NOT NULL",
      "end_date": "INTEGER NOT NULL"
    },
    "descr": "From calendar.txt, lists all the working days for each agency."
  },
  {
    "table": "Forms",
    "schema": {
      "id": "INTEGER PRIMARY KEY",
      "shape_id": "TEXT UNIQUE NOT NULL"
    },
    "descr": "From shapes.txt, lists all the unique shape_ids for each agency. shape_id syntax : 'agency-shape_id_val' "
  },
  {
    "table": "Routes",
    "schema": {
      "id": "INTEGER PRIMARY KEY",
      "route_id": "TEXT UNIQUE NOT NULL",
      "route_long_name": "TEXT NOT NULL",
      "route_type": "INTEGER NOT NULL",
      "route_color": "TEXT NOT NULL",
      "route_text_color": "TEXT NOT NULL"
    },
    "descr": "Data routes.txt, lists all the available routes for each agency."
  },
  {
    "table": "Shapes",
    "schema": {
      "id": "INTEGER PRIMARY KEY",
      "shape_id": "TEXT NOT NULL REFERENCES Forms(shape_id)",
      "lat": "REAL NOT NULL",
      "long": "REAL NOT NULL",
      "sequence": "INTEGER NOT NULL"
    },
    "descr": "From shapes.txt, lists all the points to form a shape for each route of each agency."
  },
  {
    "table": "StopTimes",
    "schema": {
      "id": "INTEGER PRIMARY KEY",
      "trip_id": "TEXT NOT NULL REFERENCES Trips(trip_id)",
      "arrival_time": "TEXT NOT NULL",
      "departure_time": "TEXT NOT NULL",
      "stop_id": "TEXT NOT NULL REFERENCES Stops(stop_id)",
      "stop_seq": "INTEGER NOT NULL"
    },
    "descr": "From stop_times.txt, lists all the stop times for each trip of each agency."
  },
  {
    "table": "Stops",
    "schema": {
      "id": "INTEGER PRIMARY KEY",
      "stop_id": "TEXT NOT NULL",
      "stop_name": "TEXT NOT NULL",
      "lat": "REAL NOT NULL",
      "long": "REAL NOT NULL",
      "stop_code": "TEXT NOT NULL",
      "wheelchair": "INTEGER NOT NULL"
    },
    "descr": "From stops.txt, lists all the available stops for each agency."
  },
  {
    "table": "Trips",
    "schema": {
      "id": "INTEGER PRIMARY KEY",
      "trip_id": "TEXT NOT NULL",
      "route_id": "TEXT NOT NULL REFERENCES Routes(route_id)",
      "service_id": "TEXT NOT NULL REFERENCES Calendar(service_id)",
      "trip_headsign": "TEXT NOT NULL",
      "direction_id": "INTEGER NOT NULL",
      "shape_id": "TEXT NOT NULL REFERENCES Forms(shape_id)",
      "wheelchair": "INTEGER NOT NULL"
    },
    "descr": "From trips.txt, lists all the available trips for each agency."
  }
]
```