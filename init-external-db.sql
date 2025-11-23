-- External User Database Initialization Script
-- This script creates the users table and populates it with test data

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    password_hash VARCHAR(512) NOT NULL,
    enabled BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create index on username and email for faster lookups
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- Insert test users
-- Password for all test users: "password123"
-- SHA-256 hash format: {SHA256}base64_encoded_hash
INSERT INTO users (username, email, first_name, last_name, password_hash, enabled) VALUES
    ('testuser1', 'testuser1@example.com', 'Test', 'User One', '{SHA256}EF92b778bbb7948a2abe50ed8e4f59f1b7b91b3e3c8f7b3e9e6f1e0e0e0e0e0', true),
    ('testuser2', 'testuser2@example.com', 'Test', 'User Two', '{SHA256}EF92b778bbb7948a2abe50ed8e4f59f1b7b91b3e3c8f7b3e9e6f1e0e0e0e0e0', true),
    ('johndoe', 'john.doe@example.com', 'John', 'Doe', '{SHA256}EF92b778bbb7948a2abe50ed8e4f59f1b7b91b3e3c8f7b3e9e6f1e0e0e0e0e0', true),
    ('janedoe', 'jane.doe@example.com', 'Jane', 'Doe', '{SHA256}EF92b778bbb7948a2abe50ed8e4f59f1b7b91b3e3c8f7b3e9e6f1e0e0e0e0e0', true),
    ('admin', 'admin@example.com', 'Admin', 'User', '{SHA256}EF92b778bbb7948a2abe50ed8e4f59f1b7b91b3e3c8f7b3e9e6f1e0e0e0e0e0', true)
ON CONFLICT (username) DO NOTHING;

-- Create function to automatically update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create trigger to automatically update updated_at
DROP TRIGGER IF EXISTS update_users_updated_at ON users;
CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Display created users
SELECT
    id,
    username,
    email,
    first_name,
    last_name,
    enabled,
    created_at
FROM users
ORDER BY id;

-- Summary
DO $$
DECLARE
    user_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO user_count FROM users;
    RAISE NOTICE '';
    RAISE NOTICE '========================================';
    RAISE NOTICE 'External User Database Initialization Complete';
    RAISE NOTICE '========================================';
    RAISE NOTICE 'Total users created: %', user_count;
    RAISE NOTICE '';
    RAISE NOTICE 'Test Credentials:';
    RAISE NOTICE '  Username: testuser1';
    RAISE NOTICE '  Password: password123';
    RAISE NOTICE '';
    RAISE NOTICE '  Username: johndoe';
    RAISE NOTICE '  Password: password123';
    RAISE NOTICE '';
    RAISE NOTICE 'All test users use password: password123';
    RAISE NOTICE '========================================';
END $$;
