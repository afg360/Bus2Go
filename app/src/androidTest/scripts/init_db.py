#!/bin/python3

import os
import sqlite3
import random

#TODO NOT DONE...

DB_FILE = "../../main/assets/database/stm_info.db"
TEST_DB_FILE = "../assets/database/stm_info_test.db"
SEED = 319284

def copy_schema_and_data():
    """Copies schema and necessary data from the original database to the test database."""
    if not os.path.exists(DB_FILE):
        print("STM database file doesn't exist. You need to initialise it.")
        exit(1)

    source_conn = sqlite3.connect(DB_FILE)
    target_conn = sqlite3.connect(TEST_DB_FILE)
    
    source_cursor = source_conn.cursor()
    target_cursor = target_conn.cursor()

    #copy the whole schema (including indexes and shit)
    source_cursor.execute("SELECT sql FROM sqlite_master WHERE sql IS NOT NULL AND type IN ('table', 'view', 'index', 'trigger') AND name NOT LIKE 'sqlite_%'")
    items = source_cursor.fetchall()


    for name, _, sql in items:
        if name != 'sqlite_sequence':
            target_cursor.execute(sql)
    
    target_conn.commit()

    #add data to the tables
    insert_statements = [
        ("Calendar", None),
        ("CalendarDates", None),
        ("Routes", None),
        ("Forms", None), #not for now...
        ("Shapes", 10), #not for now...
        ("Stops", 900),
        ("Trips", 1700),
        ("StopsInfo", 10000),
    ]

    random.seed(SEED)
    for table, limit in insert_statements:
        data = []
        if limit is not None:
            offset = 0
            chunk_size = 1000000
            while True:
                query = f"SELECT * FROM {table} LIMIT {chunk_size} OFFSET {offset};"
                
                source_cursor.execute(query)
                batch = source_cursor.fetchall()
                
                if batch is None:
                    break

                random.sample(batch, k=limit )
                target_cursor.executemany(query, batch)
                target_conn.commit()
                
                offset += chunk_size
            pass
        else:
            data = source_cursor.execute(f"SELECT * FROM {table};").fetchall()

            assert(len(data) > 0)
            columns = [desc[0] for desc in source_cursor.description]
            column_names = ", ".join(columns)
            placeholders = ", ".join(["?"] * len(columns))

            insert_query = f"INSERT INTO {table} ({column_names}) VALUES ({placeholders})"
            target_cursor.executemany(insert_query, data)
            target_conn.commit()
            target_cursor.execute(f"INSERT INTO {table} VALUES {data}")

    print("Schema and data copied successfully.")

if __name__ == "__main__":
    os.makedirs(TEST_DB_FILE)
    with sqlite3.connect(TEST_DB_FILE) as conn:
        conn.execute("VACUUM;")
    copy_schema_and_data()
    print(f"Test database created at {TEST_DB_FILE}")
