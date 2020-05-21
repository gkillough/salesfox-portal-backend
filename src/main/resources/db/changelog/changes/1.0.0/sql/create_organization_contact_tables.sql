-- Begin organization_account_contacts
create table portal.organization_account_contacts (
    contact_id uuid NOT NULL DEFAULT uuid_generate_v4(),
    organization_account_id uuid NOT NULL,
    first_name character varying,
    last_name character varying,
    email character varying,
    is_active boolean DEFAULT true
);

alter table portal.organization_account_contacts OWNER TO root;

alter table ONLY portal.organization_account_contacts
    ADD CONSTRAINT organization_account_contacts_pkey PRIMARY KEY (contact_id);

alter table portal.organization_account_contacts
    add CONSTRAINT organization_account_contacts_organization_account_id_fk FOREIGN KEY (organization_account_id) REFERENCES portal.organization_accounts(organization_account_id);
-- End organization_account_contacts

-- Begin organization_account_contact_addresses
create table portal.organization_account_contact_addresses (
    address_id uuid NOT NULL DEFAULT uuid_generate_v4(),
    contact_id uuid UNIQUE NOT NULL,
    street_number integer,
    street_name character varying,
    apt_suite character varying,
    city character varying,
    state character varying(2),
    zip_code character varying(10),
    is_business boolean
);

alter table portal.organization_account_contact_addresses OWNER TO root;

alter table ONLY portal.organization_account_contact_addresses
    ADD CONSTRAINT organization_account_contact_addresses_pkey PRIMARY KEY (address_id);

alter table portal.organization_account_contact_addresses
    add CONSTRAINT organization_account_contact_addresses_contact_id_fk FOREIGN KEY (contact_id) REFERENCES portal.organization_account_contacts(contact_id);
-- End organization_account_contact_addresses

-- Begin organization_account_contact_profiles
create table portal.organization_account_contact_profiles (
    profile_id uuid NOT NULL DEFAULT uuid_generate_v4(),
    contact_id uuid UNIQUE NOT NULL,
    contact_address_id uuid UNIQUE NOT NULL,
    organization_point_of_contact_user_id uuid,
    contact_organization_name character varying,
    title character varying,
    business_number character varying,
    mobile_number character varying
);

alter table portal.organization_account_contact_profiles OWNER TO root;

alter table ONLY portal.organization_account_contact_profiles
    ADD CONSTRAINT organization_account_contact_profiles_pkey PRIMARY KEY (profile_id);

alter table portal.organization_account_contact_profiles
    add CONSTRAINT organization_account_contact_profiles_contact_id_fk FOREIGN KEY (contact_id) REFERENCES portal.organization_account_contacts(contact_id);

alter table portal.organization_account_contact_profiles
    add CONSTRAINT organization_account_contact_profiles_contact_address_id_fk FOREIGN KEY (contact_address_id) REFERENCES portal.organization_account_contact_addresses(address_id);

alter table portal.organization_account_contact_profiles
    add CONSTRAINT organization_account_contact_profiles_organization_point_of_contact_user_id_fk FOREIGN KEY (organization_point_of_contact_user_id) REFERENCES portal.users(user_id);
-- End organization_account_contact_profiles

-- Begin organization_account_contact_interactions
create table portal.organization_account_contact_interactions (
    contact_id uuid NOT NULL,
    contact_initiations bigint DEFAULT 0,
    engagements_generated bigint DEFAULT 0
);

alter table portal.organization_account_contact_interactions OWNER TO root;

alter table ONLY portal.organization_account_contact_interactions
    ADD CONSTRAINT organization_account_contact_interactions_pkey PRIMARY KEY (contact_id);

alter table portal.organization_account_contact_interactions
    add CONSTRAINT client_interaction_organization_account_contact_id_fk FOREIGN KEY (contact_id) REFERENCES portal.organization_account_contacts(contact_id);
-- End organization_account_contact_interactions