create table portal.licenses (
    license_id uuid NOT NULL DEFAULT uuid_generate_v4(),
    license_hash uuid,
    expiration_date date,
    type character varying,
    available_license_seats bigint,
    max_license_seats bigint,
    monthly_cost double precision,
    is_active boolean DEFAULT true
);

alter table portal.licenses OWNER TO root;

alter table ONLY portal.licenses
    ADD CONSTRAINT licenses_license_hash_key UNIQUE (license_hash);

alter table ONLY portal.licenses
    ADD CONSTRAINT licenses_pkey PRIMARY KEY (license_id);

create table portal.logins (
    login_id uuid NOT NULL DEFAULT uuid_generate_v4(),
    user_id uuid,
    password_hash character varying,
    last_successful_login timestamp without time zone,
    last_locked timestamp without time zone,
    num_failed_logins integer
);

alter table portal.logins OWNER TO root;

alter table ONLY portal.logins
    ADD CONSTRAINT logins_pkey PRIMARY KEY (login_id);

create table portal.memberships (
    membership_id uuid NOT NULL DEFAULT uuid_generate_v4(),
    user_id uuid,
    organization_account_id uuid,
    role_id uuid
);

alter table portal.memberships OWNER TO root;

alter table ONLY portal.memberships
    ADD CONSTRAINT memberships_pkey PRIMARY KEY (membership_id);

create table portal.organization_account_addresses (
    organization_account_address_id uuid NOT NULL DEFAULT uuid_generate_v4(),
    organization_account_id uuid UNIQUE NOT NULL,
    street_number integer,
    street_name character varying,
    apt_suite character varying,
    city character varying,
    state character varying(2),
    zip_code character varying(10),
    is_business boolean
);

alter table portal.organization_account_addresses OWNER TO root;

alter table ONLY portal.organization_account_addresses
    ADD CONSTRAINT organization_account_addresses_pkey PRIMARY KEY (organization_account_address_id);

create table portal.organization_accounts (
    organization_account_id uuid NOT NULL DEFAULT uuid_generate_v4(),
    organization_account_name character varying,
    license_id uuid,
    organization_id uuid,
    is_active boolean DEFAULT true
);

alter table portal.organization_accounts OWNER TO root;

alter table ONLY portal.organization_accounts
    ADD CONSTRAINT organization_accounts_pkey PRIMARY KEY (organization_account_id);

alter table ONLY portal.organization_accounts
    ADD CONSTRAINT organization_accounts_organization_account_name_with_id_key UNIQUE (organization_id, organization_account_name);

alter table portal.organization_account_addresses
    add CONSTRAINT organization_account_id_fk FOREIGN KEY (organization_account_id) REFERENCES portal.organization_accounts(organization_account_id);


create table portal.organizations (
    organization_id uuid NOT NULL DEFAULT uuid_generate_v4(),
    organization_name character varying,
    is_active boolean DEFAULT true
);

alter table portal.organizations OWNER TO root;

alter table ONLY portal.organizations
    ADD CONSTRAINT organizations_pkey PRIMARY KEY (organization_id);

create table portal.profiles (
    profile_id uuid NOT NULL DEFAULT uuid_generate_v4(),
    user_id uuid UNIQUE NOT NULL,
    mobile_number character varying,
    business_number character varying,
    mailing_address_id uuid
);

alter table portal.profiles OWNER TO root;

alter table ONLY portal.profiles
    ADD CONSTRAINT profiles_pkey PRIMARY KEY (profile_id);

create table portal.roles (
    role_id uuid NOT NULL DEFAULT uuid_generate_v4(),
    role_level character varying,
    role_description character varying,
    is_role_restricted boolean
);

alter table portal.roles OWNER TO root;

alter table ONLY portal.roles
    ADD CONSTRAINT roles_pkey PRIMARY KEY (role_id);

alter table ONLY portal.roles
    ADD CONSTRAINT roles_role_level_key UNIQUE (role_level);

create table portal.user_addresses (
    user_address_id uuid NOT NULL DEFAULT uuid_generate_v4(),
    user_id uuid UNIQUE NOT NULL,
    street_number integer,
    street_name character varying,
    apt_suite character varying,
    city character varying,
    state character varying(2),
    zip_code character varying(10),
    is_business boolean
);

alter table portal.user_addresses OWNER TO root;

alter table ONLY portal.user_addresses
    ADD CONSTRAINT user_addresses_pkey PRIMARY KEY (user_address_id);

create table portal.users (
    user_id uuid NOT NULL DEFAULT uuid_generate_v4(),
    email character varying NOT NULL,
    first_name character varying,
    last_name character varying,
    is_active boolean DEFAULT true
);

alter table portal.users OWNER TO root;

alter table ONLY portal.users
    ADD CONSTRAINT users_email_key UNIQUE (email);

alter table ONLY portal.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (user_id);

alter table portal.user_addresses
    add CONSTRAINT user_id_fk FOREIGN KEY (user_id) REFERENCES portal.users(user_id);

create table portal.password_reset_tokens (
    email character varying NOT NULL,
    token character varying NOT NULL,
    date_generated timestamp NOT NULL
);

alter table portal.password_reset_tokens OWNER TO root;

alter table ONLY portal.password_reset_tokens
    ADD CONSTRAINT password_reset_tokens_pkey PRIMARY KEY (email, token);
