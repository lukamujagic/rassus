CREATE DATABASE IF NOT EXISTS humidity_database;

USE humidity_database;

CREATE TABLE IF NOT EXISTS humidity_data (
    row_id INTEGER PRIMARY KEY,
    humidity_value INTEGER
);