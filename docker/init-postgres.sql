-- PostgreSQL initialization script
-- Creates databases for member and cart services

-- Create marketplace_member database
CREATE DATABASE marketplace_member;

-- Create marketplace_cart database
CREATE DATABASE marketplace_cart;

-- Grant privileges to postgres user (default user)
GRANT ALL PRIVILEGES ON DATABASE marketplace_member TO postgres;
GRANT ALL PRIVILEGES ON DATABASE marketplace_cart TO postgres;
