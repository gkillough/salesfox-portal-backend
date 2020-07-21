DO $$
BEGIN
    -- install dblink extension
    CREATE EXTENSION IF NOT EXISTS dblink;
    -- create role and database if there are errors they will be logged.
    PERFORM dblink('user=root dbname=postgres', 'CREATE ROLE root LOGIN PASSWORD ''root'' ', FALSE);
    PERFORM dblink('user=root dbname=postgres', 'CREATE DATABASE boostr WITH OWNER root', FALSE);
END
$$;
