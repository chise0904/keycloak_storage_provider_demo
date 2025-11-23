#!/bin/bash

echo "========================================="
echo "Keycloak External User Storage Provider"
echo "Status Check Script"
echo "========================================="
echo ""

# Color codes
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

check_command() {
    if command -v $1 &> /dev/null; then
        echo -e "${GREEN}✓${NC} $1 is installed"
        return 0
    else
        echo -e "${RED}✗${NC} $1 is NOT installed"
        return 1
    fi
}

check_file() {
    if [ -f "$1" ]; then
        echo -e "${GREEN}✓${NC} File exists: $1"
        return 0
    else
        echo -e "${RED}✗${NC} File missing: $1"
        return 1
    fi
}

check_docker_container() {
    if docker ps | grep -q $1; then
        echo -e "${GREEN}✓${NC} Container running: $1"
        return 0
    else
        echo -e "${RED}✗${NC} Container NOT running: $1"
        return 1
    fi
}

# Check prerequisites
echo "1. Checking Prerequisites..."
echo "----------------------------"
check_command java
check_command mvn
check_command docker
check_command docker-compose
echo ""

# Check build
echo "2. Checking Build..."
echo "----------------------------"
check_file "pom.xml"
check_file "target/external-user-storage-provider-1.0.0.jar"
check_file "keycloak-providers/external-user-storage-provider-1.0.0.jar"
echo ""

# Check Docker containers
echo "3. Checking Docker Containers..."
echo "----------------------------"
if docker-compose ps &> /dev/null; then
    check_docker_container "traefik"
    check_docker_container "keycloak-postgres"
    check_docker_container "external-user-db"
    check_docker_container "keycloak"
    check_docker_container "demo-app"
else
    echo -e "${YELLOW}⚠${NC} Docker Compose not running"
fi
echo ""

# Check Keycloak provider
echo "4. Checking Keycloak Provider..."
echo "----------------------------"
if docker exec keycloak ls /opt/keycloak/providers/external-user-storage-provider-1.0.0.jar &> /dev/null; then
    echo -e "${GREEN}✓${NC} Provider JAR deployed in Keycloak"
else
    echo -e "${RED}✗${NC} Provider JAR NOT found in Keycloak"
fi
echo ""

# Check database
echo "5. Checking External Database..."
echo "----------------------------"
if docker exec external-user-db psql -U userapp -d userdb -c "SELECT COUNT(*) FROM users;" &> /dev/null; then
    USER_COUNT=$(docker exec external-user-db psql -U userapp -d userdb -t -c "SELECT COUNT(*) FROM users;")
    echo -e "${GREEN}✓${NC} Database accessible"
    echo -e "${GREEN}✓${NC} Total users: $USER_COUNT"
else
    echo -e "${RED}✗${NC} Database NOT accessible"
fi
echo ""

# Check Keycloak logs
echo "6. Checking Keycloak Logs..."
echo "----------------------------"
if docker-compose logs keycloak 2>/dev/null | grep -q "External User Storage Provider"; then
    echo -e "${GREEN}✓${NC} Provider initialization logged"
else
    echo -e "${YELLOW}⚠${NC} Provider initialization not found in logs"
fi

if docker-compose logs keycloak 2>/dev/null | grep -qi "error.*external"; then
    echo -e "${RED}✗${NC} Errors found in logs"
    echo "Run: docker-compose logs keycloak | grep -i error"
else
    echo -e "${GREEN}✓${NC} No provider errors in logs"
fi
echo ""

# Summary
echo "========================================="
echo "Quick Commands:"
echo "========================================="
echo "Build:         ./build.sh"
echo "Deploy:        ./deploy.sh"
echo "Start:         docker-compose up -d"
echo "Logs:          docker-compose logs -f keycloak"
echo "Stop:          docker-compose down"
echo "DB Shell:      docker exec -it external-user-db psql -U userapp -d userdb"
echo "Test Login:    https://auth.lovejulian.shop/realms/master/account"
echo ""
echo "Test Credentials:"
echo "  Username: testuser1"
echo "  Password: password123"
echo "========================================="
