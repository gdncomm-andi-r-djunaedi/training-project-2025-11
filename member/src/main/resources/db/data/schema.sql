DROP TABLE IF EXISTS members;

CREATE TABLE members (
    id UUID PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20),
    avatar_url VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
