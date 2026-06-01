-- Student Result Management System — database schema
-- Run: mysql -u root -p < schema.sql
-- Or create database manually and let the app run setupTables() on first launch.

CREATE DATABASE IF NOT EXISTS student_result_db;
USE student_result_db;

CREATE TABLE IF NOT EXISTS students (
    id INT AUTO_INCREMENT PRIMARY KEY,
    roll_number VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    subject1 FLOAT,
    subject2 FLOAT,
    subject3 FLOAT,
    subject4 FLOAT,
    subject5 FLOAT,
    total FLOAT,
    percentage FLOAT,
    grade VARCHAR(5),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS subject_config (
    id INT PRIMARY KEY,
    subject_name VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL
);

INSERT IGNORE INTO subject_config (id, subject_name) VALUES
    (1, 'Subject 1'), (2, 'Subject 2'), (3, 'Subject 3'),
    (4, 'Subject 4'), (5, 'Subject 5');

-- Default admin password is set by the application (hashed) on first run.
