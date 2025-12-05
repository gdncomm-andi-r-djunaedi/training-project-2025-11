-- Create separate databases for member and cart services
CREATE DATABASE member_db;
CREATE DATABASE cart_db;

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE member_db TO ecommerce_user;
GRANT ALL PRIVILEGES ON DATABASE cart_db TO ecommerce_user;
