create table portal.clients (
    client_id bigserial NOT NULL,
    first_name character varying,
    last_name character varying,
    email character varying,
    is_active boolean DEFAULT true
);

alter table portal.clients OWNER TO root;

alter table ONLY portal.clients
    ADD CONSTRAINT clients_pkey PRIMARY KEY (client_id);

create table portal.client_addresses (
    client_address_id bigserial NOT NULL,
    client_id bigint UNIQUE NOT NULL,
    street_number integer,
    street_name character varying,
    apt_suite character varying,
    city character varying,
    state character varying(2),
    zip_code character varying(10),
    is_business boolean
);

alter table portal.client_addresses OWNER TO root;

alter table ONLY portal.client_addresses
    ADD CONSTRAINT client_addresses_pkey PRIMARY KEY (client_address_id);

alter table portal.client_addresses
    add CONSTRAINT client_address_client_id_fk FOREIGN KEY (client_id) REFERENCES portal.clients(client_id);

create table portal.client_profiles (
    profile_id bigserial NOT NULL,
    client_id bigserial UNIQUE NOT NULL,
    client_address_id bigserial UNIQUE NOT NULL,
    organization_name character varying,
    title character varying,
    organization_point_of_contact character varying,
    business_number character varying,
    mobile_number character varying
);

alter table portal.client_profiles OWNER TO root;

alter table ONLY portal.client_profiles
    ADD CONSTRAINT client_profiles_pkey PRIMARY KEY (profile_id);

alter table portal.client_profiles
    add CONSTRAINT client_profile_client_id_fk FOREIGN KEY (client_id) REFERENCES portal.clients(client_id);

alter table portal.client_profiles
    add CONSTRAINT client_profile_client_address_id_fk FOREIGN KEY (client_address_id) REFERENCES portal.client_addresses(client_address_id);

create table portal.client_interactions (
    client_id bigint NOT NULL,
    contact_initiations bigint DEFAULT 0,
    engagements_generated bigint DEFAULT 0
);

alter table portal.client_interactions OWNER TO root;

alter table ONLY portal.client_interactions
    ADD CONSTRAINT client_interactions_pkey PRIMARY KEY (client_id);

alter table portal.client_interactions
    add CONSTRAINT client_interaction_client_id_fk FOREIGN KEY (client_id) REFERENCES portal.clients(client_id);