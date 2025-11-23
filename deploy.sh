#!/bin/bash
set -e

echo "========================================="
echo "Deploying External User Storage Provider"
echo "========================================="

# Check if JAR file exists
JAR_FILE="target/external-user-storage-provider-1.0.0.jar"
if [ ! -f "$JAR_FILE" ]; then
    echo "Error: JAR file not found: $JAR_FILE"
    echo "Please run ./build.sh first"
    exit 1
fi

# Create keycloak-providers directory if it doesn't exist
echo "Creating providers directory..."
mkdir -p ./keycloak-providers

# Copy JAR to providers directory
echo "Copying JAR to providers directory..."
cp "$JAR_FILE" ./keycloak-providers/

echo ""
echo "✅ Deployment successful!"
echo ""
echo "Provider JAR copied to: ./keycloak-providers/"
echo ""
echo "Next steps:"
echo "1. Restart Keycloak: docker-compose restart keycloak"
echo "2. Wait for Keycloak to start (check logs: docker-compose logs -f keycloak)"
echo "3. Access Keycloak Admin Console: https://auth.lovejulian.shop"
echo "4. Configure the provider in: Realm Settings > User Federation > Add Provider"
echo ""
