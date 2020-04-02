create table portal.organization_account_profiles (
    profile_id bigserial NOT NULL,
    organization_account_id bigint NOT NULL,
    mailing_address_id bigint NOT NULL,
    business_number character varying
);

alter table portal.organization_account_profiles OWNER TO root;

alter table ONLY portal.organization_account_profiles
    ADD CONSTRAINT organization_account_profiles_pkey PRIMARY KEY (profile_id);

alter table portal.organization_account_profiles
    add CONSTRAINT organization_account_profile_organization_account_id_fk FOREIGN KEY (organization_account_id) REFERENCES portal.organization_accounts(organization_account_id);

alter table portal.organization_account_profiles
    add CONSTRAINT organization_account_profiles_mailing_address_id_fk FOREIGN KEY (mailing_address_id) REFERENCES portal.organization_account_addresses(organization_account_address_id);