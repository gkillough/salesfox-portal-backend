DO $$
BEGIN
    -- install dblink extension
    CREATE EXTENSION IF NOT EXISTS dblink;
    -- create role and database if there are errors they will be logged.
    PERFORM dblink('user=root dbname=postgres', 'CREATE ROLE portaladmin WITH SUPERUSER LOGIN ENCRYPTED PASSWORD ''Port@l!23'' ', FALSE);
    PERFORM dblink('user=portaladmin dbname=postgres', 'CREATE DATABASE boostr WITH OWNER portaladmin', FALSE);
    --PERFORM dblink('user=portaladmin dbname=boostr', 'CREATE SCHEMA portal', FALSE);
    --PERFORM dblink('user=portaladmin dbname=boostr', 'CREATE ROLE portaluser WITH LOGIN PASSWORD ''Port@l!23'' ', FALSE);
    --PERFORM dblink('user=portaladmin dbname=boostr', 'GRANT USAGE ON SCHEMA portal TO portaluser', FALSE);
    --PERFORM dblink('user=portaladmin dbname=boostr', 'ALTER DEFAULT PRIVILEGES IN SCHEMA portal GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO portaluser', FALSE);
END
$$;
