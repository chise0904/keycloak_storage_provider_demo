#!/bin/bash
set -e

echo "========================================="
echo "Building External User Storage Provider"
echo "========================================="

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not installed"
    echo "Please install Maven: brew install maven (macOS) or apt-get install maven (Linux)"
    exit 1
fi

# Clean previous builds
echo "Cleaning previous builds..."
mvn clean

# Build the project
echo "Building JAR file..."
mvn package -DskipTests

# Check if build was successful
if [ -f "target/external-user-storage-provider-1.0.0.jar" ]; then
    echo ""
    echo "✅ Build successful!"
    echo "JAR file: target/external-user-storage-provider-1.0.0.jar"
    echo ""
    echo "Next steps:"
    echo "1. Run ./deploy.sh to deploy to Keycloak"
    echo "2. Restart Keycloak container: docker-compose restart keycloak"
else
    echo ""
    echo "❌ Build failed!"
    exit 1
fi
