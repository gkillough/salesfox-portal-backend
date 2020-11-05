package ai.salesfox.portal.rest.api.organization.common;

public class OrganizationEndpointConstants {
    public static final String BASE_ENDPOINT = "/organization";
    @Deprecated(forRemoval = true)
    // FIXME eventually replace this with "/accounts"
    public static final String ACCOUNT_ENDPOINT = BASE_ENDPOINT + "/account";
    public static final String ACCOUNTS_ENDPOINT = BASE_ENDPOINT + "/accounts";

}
