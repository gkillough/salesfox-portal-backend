create table portal.clients (
    client_id bigserial NOT NULL,
    first_name character varying,
    last_name character varying,
    organization_name character varying,
    title character varying,
    email character varying,
    phone_number character varying,
    organization_point_of_contact character varying,
    is_active boolean DEFAULT true
);

alter table portal.clients OWNER TO root;

alter table ONLY portal.clients
    ADD CONSTRAINT clients_pkey PRIMARY KEY (client_id);