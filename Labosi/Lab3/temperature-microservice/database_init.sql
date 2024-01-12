CREATE DATABASE IF NOT EXISTS temperature_database;

USE temperature_database;

CREATE TABLE IF NOT EXISTS temperature_data (
    row_id INTEGER PRIMARY KEY,
    temperature_value INTEGER
);