# Keycloak External User Storage Provider - Setup Guide

Complete guide for setting up the custom Keycloak User Storage Provider.

## Quick Start

### 1. Build the Provider

```bash
./build.sh
```

### 2. Deploy to Keycloak

```bash
./deploy.sh
```

### 3. Start Docker Environment

```bash
docker-compose up -d
```

### 4. Monitor Keycloak Startup

```bash
docker-compose logs -f keycloak
```

Wait for message: `Keycloak ... started`

### 5. Access Keycloak Admin

URL: `https://auth.lovejulian.shop`
- Username: `admin`
- Password: `admin123`

### 6. Configure User Federation

1. Go to: **User Federation** in your realm
2. Click: **Add provider** → **external-user-storage**
3. Configure:
   - Display Name: `External User Database`
   - Database Host: `external-user-db`
   - Database Port: `5432`
   - Database Name: `userdb`
   - Database User: `userapp`
   - Database Password: `userapp_password`
4. Click **Save**

### 7. Test Authentication

Try logging in with:
- Username: `testuser1`
- Password: `password123`

## Test Users

| Username   | Email                    | Password     |
|------------|--------------------------|--------------|
| testuser1  | testuser1@example.com    | password123  |
| johndoe    | john.doe@example.com     | password123  |
| janedoe    | jane.doe@example.com     | password123  |

## Troubleshooting

### Check Provider is Loaded

```bash
docker exec keycloak ls -l /opt/keycloak/providers/
```

### View Logs

```bash
docker-compose logs -f keycloak
```

### Test Database Connection

```bash
docker exec external-user-db psql -U userapp -d userdb -c "SELECT username, email FROM users;"
```

### Restart Keycloak

```bash
docker-compose restart keycloak
```

## Next Steps

See [README.md](README.md) for complete documentation.
