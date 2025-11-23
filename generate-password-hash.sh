#!/bin/bash

# Generate SHA-256 password hash for testing
# Usage: ./generate-password-hash.sh "password123"

PASSWORD="${1:-password123}"

# Generate SHA-256 hash and encode in base64
HASH=$(echo -n "$PASSWORD" | shasum -a 256 | cut -d' ' -f1 | xxd -r -p | base64)

echo "Password: $PASSWORD"
echo "SHA-256 Hash: {SHA256}$HASH"
echo ""
echo "Use this in your SQL:"
echo "INSERT INTO users (username, email, password_hash) VALUES ('user', 'user@example.com', '{SHA256}$HASH');"
