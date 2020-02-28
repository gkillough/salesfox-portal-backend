CREATE TABLE portal.licenses (
    license_id bigserial NOT NULL,
    license_hash uuid,
    expiration_date date,
    type character varying,
    license_seats bigint,
    monthly_cost double precision,
    is_active boolean DEFAULT true
);

ALTER TABLE portal.licenses OWNER TO root;

ALTER TABLE ONLY portal.licenses
    ADD CONSTRAINT licenses_license_hash_key UNIQUE (license_hash);

ALTER TABLE ONLY portal.licenses
    ADD CONSTRAINT licenses_pkey PRIMARY KEY (license_id);


CREATE TABLE portal.logins (
    login_id bigserial NOT NULL,
    user_id bigint,
    password_hash character varying,
    last_successful_login date,
    num_failed_logins integer
);

ALTER TABLE portal.logins OWNER TO root;

ALTER TABLE ONLY portal.logins
    ADD CONSTRAINT logins_pkey PRIMARY KEY (login_id);

CREATE TABLE portal.memberships (
    membership_id bigserial NOT NULL,
    user_id bigint,
    organization_account_id bigint,
    role_id bigint,
    is_active boolean DEFAULT true
);

ALTER TABLE portal.memberships OWNER TO root;

ALTER TABLE ONLY portal.memberships
    ADD CONSTRAINT memberships_pkey PRIMARY KEY (membership_id);

CREATE TABLE portal.organization_account_addresses (
    organization_account_address_id bigserial NOT NULL,
    organization_account_id bigint,
    street_number integer,
    street_name character varying,
    apt_suite character varying,
    city character varying,
    state character varying(2),
    zip_code character varying(10),
    is_business boolean
);

ALTER TABLE portal.organization_account_addresses OWNER TO root;

ALTER TABLE ONLY portal.organization_account_addresses
    ADD CONSTRAINT organization_account_addresses_pkey PRIMARY KEY (organization_account_address_id);

CREATE TABLE portal.organization_accounts (
    organization_account_id bigserial NOT NULL,
    organization_account_name character varying,
    license_id bigint,
    organization_id bigint,
    is_active boolean DEFAULT true
);

ALTER TABLE portal.organization_accounts OWNER TO root;

ALTER TABLE ONLY portal.organization_accounts
    ADD CONSTRAINT organization_accounts_organization_account_name_key UNIQUE (organization_account_name);

ALTER TABLE ONLY portal.organization_accounts
    ADD CONSTRAINT organization_accounts_pkey PRIMARY KEY (organization_account_id);

CREATE TABLE portal.organizations (
    organization_id bigserial NOT NULL,
    organization_name character varying,
    is_active boolean DEFAULT true
);

ALTER TABLE portal.organizations OWNER TO root;

ALTER TABLE ONLY portal.organizations
    ADD CONSTRAINT organizations_pkey PRIMARY KEY (organization_id);

CREATE TABLE portal.profiles (
    profile_id bigserial NOT NULL,
    user_id bigint,
    mobile_number character varying,
    business_number character varying,
    mailing_address_id bigint
);

ALTER TABLE portal.profiles OWNER TO root;

ALTER TABLE ONLY portal.profiles
    ADD CONSTRAINT profiles_pkey PRIMARY KEY (profile_id);

CREATE TABLE portal.roles (
    role_id bigserial NOT NULL,
    role_level character varying,
    role_description character varying,
    is_role_restricted boolean
);

ALTER TABLE portal.roles OWNER TO root;

ALTER TABLE ONLY portal.roles
    ADD CONSTRAINT roles_pkey PRIMARY KEY (role_id);

ALTER TABLE ONLY portal.roles
    ADD CONSTRAINT roles_role_level_key UNIQUE (role_level);

CREATE TABLE portal.user_addresses (
    user_address_id bigserial NOT NULL,
    street_number integer,
    street_name character varying,
    apt_suite character varying,
    city character varying,
    state character varying(2),
    zip_code character varying(10),
    is_business boolean
);

ALTER TABLE portal.user_addresses OWNER TO root;

ALTER TABLE ONLY portal.user_addresses
    ADD CONSTRAINT user_addresses_pkey PRIMARY KEY (user_address_id);

CREATE TABLE portal.users (
    user_id bigserial NOT NULL,
    email character varying NOT NULL,
    first_name character varying,
    last_name character varying,
    is_active boolean DEFAULT true
);

ALTER TABLE portal.users OWNER TO root;

ALTER TABLE ONLY portal.users
    ADD CONSTRAINT users_email_key UNIQUE (email);

ALTER TABLE ONLY portal.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (user_id);

CREATE TABLE portal.password_reset_tokens (
    email character varying NOT NULL,
    token character varying NOT NULL,
    date_generated date NOT NULL
);

ALTER TABLE portal.password_reset_tokens OWNER TO root;

ALTER TABLE ONLY portal.password_reset_tokens
    ADD CONSTRAINT password_reset_tokens_pkey PRIMARY KEY (email, token);
