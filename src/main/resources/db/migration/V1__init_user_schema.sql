-- V1__init_user_schema.sql

-- Users table
CREATE TABLE IF NOT EXISTS users (
     id SERIAL PRIMARY KEY,
     username VARCHAR(50) NOT NULL UNIQUE,
     password VARCHAR(255) NOT NULL,
     registration_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
     last_login_date TIMESTAMP
);


