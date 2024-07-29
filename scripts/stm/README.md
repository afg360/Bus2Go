# Python script
The python script in this directory downloads the data static data from the STM website, and initialises the database with the
appropriate data.

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
    "descr": "From calendar.txt, lists all the working days the STM."
  },
  {
    "table": "Forms",
    "schema": {
      "id": "INTEGER PRIMARY KEY",
      "shape_id": "INTEGER UNIQUE NOT NULL"
    },
    "descr": "From shapes.txt, lists all the unique shape_ids for the STM."
  },
  {
    "table": "Routes",
    "schema": {
      "id": "INTEGER PRIMARY KEY",
      "route_id": "INTEGER UNIQUE NOT NULL",
      "route_long_name": "TEXT NOT NULL",
      "route_type": "INTEGER NOT NULL",
      "route_color": "TEXT NOT NULL"
    },
    "descr": "Data routes.txt, lists all the available routes for the STM."
  },
  {
    "table": "Shapes",
    "schema": {
      "id": "INTEGER PRIMARY KEY",
      "shape_id": "INTEGER NOT NULL REFERENCES Forms(shape_id)",
      "lat": "REAL NOT NULL",
      "long": "REAL NOT NULL",
      "sequence": "INTEGER NOT NULL"
    },
    "descr": "From shapes.txt, lists all the points to form a shape for each route of the STM."
  },
  {
    "table": "StopsInfo",
    "schema": {
      "id": "INTEGER PRIMARY KEY",
      "stop_name": "TEXT NOT NULL",
      "route_id": "INTEGER NOT NULL",
      "trip_headsign": "TEXT NOT NULL",
      "days": "TEXT NOT NULL",
      "arrival_time": "TEXT NOT NULL",
      "stop_seq": "INTEGER NOT NULL"
    },
    "descr": "Data taken from stop_times.txt, trips.txt, calendar.txt and stops.txt. Lists all the stop times for each trip of the STM."
  },
  {
    "table": "Stops",
    "schema": {
      "id": "INTEGER PRIMARY KEY",
      "stop_id": "TEXT NOT NULL",
      "stop_code": "INTEGER NOT NULL",
      "stop_name": "TEXT NOT NULL",
      "lat": "REAL NOT NULL",
      "long": "REAL NOT NULL",
      "wheelchair": "INTEGER NOT NULL"
    },
    "descr": "From stops.txt, lists all the available stops for the STM."
  },
  {
    "table": "Trips",
    "schema": {
      "id": "INTEGER PRIMARY KEY",
      "trip_id": "INTEGER NOT NULL",
      "route_id": "INTEGER NOT NULL REFERENCES Routes(route_id)",
      "service_id": "TEXT NOT NULL REFERENCES Calendar(service_id)",
      #trip_headsign IS ACTUALLY A DIRECTION FOR THE STM!!!
      "trip_headsign": "TEXT NOT NULL",
      "direction_id": "INTEGER NOT NULL",
      "shape_id": "INTEGER NOT NULL REFERENCES Forms(shape_id)",
      "wheelchair": "INTEGER NOT NULL"
    },
    "descr": "From trips.txt, lists all the available trips for the STM."
  }
]
```