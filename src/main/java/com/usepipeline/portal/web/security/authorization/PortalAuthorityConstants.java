package com.usepipeline.portal.web.security.authorization;

public final class PortalAuthorityConstants {
    // Spring only allows string literals in @PreAuthorize annotations.
    // Even a String returned by a static method is not allowed here.

    // User Roles
    public static final String PIPELINE_ADMIN = "PIPELINE_ADMIN";
    public static final String PIPELINE_ADMIN_ROLE_CHECK = "hasRole('" + PIPELINE_ADMIN + "')";

    public static final String PIPELINE_BASIC_USER = "PIPELINE_BASIC_USER";
    public static final String PIPELINE_BASIC_USER_ROLE_CHECK = "hasRole('" + PIPELINE_BASIC_USER + "')";

    public static final String PIPELINE_PREMIUM_USER = "PIPELINE_PREMIUM_USER";
    public static final String PIPELINE_PREMIUM_USER_ROLE_CHECK = "hasRole('" + PIPELINE_PREMIUM_USER + "')";

    public static final String ORGANIZATION_ACCOUNT_MANAGER = "ORGANIZATION_ACCOUNT_MANAGER";
    public static final String ORGANIZATION_ACCOUNT_MANAGER_ROLE_CHECK = "hasRole('" + ORGANIZATION_ACCOUNT_MANAGER + "')";

    public static final String ORGANIZATION_SALES_REP_MANAGER = "ORGANIZATION_SALES_REP_MANAGER";
    public static final String ORGANIZATION_SALES_REP_MANAGER_ROLE_CHECK = "hasRole('" + ORGANIZATION_SALES_REP_MANAGER + "')";

    public static final String ORGANIZATION_SALES_REP = "ORGANIZATION_SALES_REP";
    public static final String ORGANIZATION_SALES_REP_ROLE_CHECK = "hasRole('" + ORGANIZATION_SALES_REP + "')";

    // Application Authorities
    // Note: https://stackoverflow.com/questions/42146110/when-should-i-prefix-role-with-spring-security
    public static final String UPDATE_PASSWORD_PERMISSION = "UPDATE_PASSWORD_PERMISSION";
    public static final String UPDATE_PASSWORD_PERMISSION_ROLE_CHECK = "hasRole('" + UPDATE_PASSWORD_PERMISSION + "')";

}
