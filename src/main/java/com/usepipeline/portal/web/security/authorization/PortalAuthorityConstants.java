package com.usepipeline.portal.web.security.authorization;

public final class PortalAuthorityConstants {
    // Spring only allows string literals in @PreAuthorize annotations.
    // Even a String returned by a static method is not allowed here.

    // Prefixes
    public static final String PIPELINE_ROLE_PREFIX = "PIPELINE_";
    public static final String ORGANIZATION_ROLE_PREFIX = "ORGANIZATION_";

    // User Roles
    public static final String ANONYMOUS = "ANONYMOUS";

    public static final String PIPELINE_ADMIN = PIPELINE_ROLE_PREFIX + "ADMIN";
    public static final String PIPELINE_ADMIN_ROLE_CHECK = "hasAuthority('" + PIPELINE_ADMIN + "')";

    public static final String PIPELINE_BASIC_USER = PIPELINE_ROLE_PREFIX + "BASIC_USER";
    public static final String PIPELINE_BASIC_USER_AUTH_CHECK = "hasAuthority('" + PIPELINE_BASIC_USER + "')";

    public static final String PIPELINE_PREMIUM_USER = PIPELINE_ROLE_PREFIX + "PREMIUM_USER";
    public static final String PIPELINE_PREMIUM_USER_AUTH_CHECK = "hasAuthority('" + PIPELINE_PREMIUM_USER + "')";

    public static final String ORGANIZATION_ACCOUNT_OWNER = ORGANIZATION_ROLE_PREFIX + "ACCOUNT_OWNER";
    public static final String ORGANIZATION_ACCOUNT_OWNER_AUTH_CHECK = "hasAuthority('" + ORGANIZATION_ACCOUNT_OWNER + "')";

    public static final String ORGANIZATION_ACCOUNT_MANAGER = ORGANIZATION_ROLE_PREFIX + "ACCOUNT_MANAGER";
    public static final String ORGANIZATION_ACCOUNT_MANAGER_AUTH_CHECK = "hasAuthority('" + ORGANIZATION_ACCOUNT_MANAGER + "')";

    public static final String ORGANIZATION_ACCOUNT_REP = ORGANIZATION_ROLE_PREFIX + "ACCOUNT_REP";
    public static final String ORGANIZATION_ACCOUNT_REP_AUTH_CHECK = "hasAuthority('" + ORGANIZATION_ACCOUNT_REP + "')";

    // Application Authorities
    public static final String UPDATE_PASSWORD_PERMISSION = "UPDATE_PASSWORD_PERMISSION";
    public static final String UPDATE_PASSWORD_PERMISSION_AUTH_CHECK = "hasAuthority('" + UPDATE_PASSWORD_PERMISSION + "')";

    public static final String CREATE_ORGANIZATION_ACCOUNT_PERMISSION = "CREATE_ORGANIZATION_ACCOUNT_PERMISSION";
    public static final String CREATE_ORGANIZATION_ACCOUNT_PERMISSION_AUTH_CHECK = "hasAuthority('" + CREATE_ORGANIZATION_ACCOUNT_PERMISSION + "')";

}
