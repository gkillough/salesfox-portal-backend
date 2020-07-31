# Boostr Portal

## Start a development instance
1. Install Docker
2. Make sure Docker is running
3. Run the following command:
```bash
./gradlew clean runServer
```

The gradle `runServer` task has two flags that can be set:
- `--reuseContainer` will keep the database from the previous run
- `--suspend` will wait for a debug connection (on port 9095) as soon as the application server starts

## Set up Postgres
1. Create _portaladmin_ role: 
```sql 
CREATE ROLE portaladmin WITH SUPERUSER LOGIN ENCRYPTED PASSWORD 'Port@l!23'
```

2. Create database: 
```sql
CREATE DATABASE boostr WITH OWNER portaladmin;
```

3. Create schema:
```sql
CREATE SCHEMA portal;
```

4. Create _portaluser_ role: 
```sql
CREATE ROLE portaluser WITH LOGIN ENCRYPTED PASSWORD 'Port@l!23'

GRANT USAGE ON SCHEMA portal TO portaluser;
ALTER DEFAULT PRIVILEGES IN SCHEMA portal GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO portaluser;
```
