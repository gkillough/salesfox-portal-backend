DO $$
BEGIN
    -- install dblink extension
    CREATE EXTENSION IF NOT EXISTS dblink;
    -- create role and database if there are errors they will be logged.
    PERFORM dblink('user=root dbname=postgres', 'CREATE ROLE portaladmin WITH SUPERUSER LOGIN ENCRYPTED PASSWORD ''Port@l!23'' ', FALSE);
    PERFORM dblink('user=portaladmin dbname=postgres', 'CREATE DATABASE salesfox WITH OWNER portaladmin', FALSE);
END
$$;
