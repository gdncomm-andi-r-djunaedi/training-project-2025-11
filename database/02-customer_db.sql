\connect customer_db;

-- Enable UUID extension for postgres
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS customer (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(50),
    address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS customer_auth (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL REFERENCES customer(id),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    last_login TIMESTAMP
);

COPY customer(id, name, email, phone, address, created_at)
FROM '/docker-entrypoint-initdb.d/customer.csv'
WITH (FORMAT csv, HEADER true);

COPY customer_auth(id, customer_id, email, password_hash, last_login)
FROM '/docker-entrypoint-initdb.d/customer_auth.csv'
WITH (FORMAT csv, HEADER true);