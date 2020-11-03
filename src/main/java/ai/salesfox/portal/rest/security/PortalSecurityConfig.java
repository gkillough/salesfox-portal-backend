package ai.salesfox.portal.rest.security;

import ai.salesfox.portal.database.account.entity.RoleEntity;
import ai.salesfox.portal.database.account.repository.RoleRepository;
import ai.salesfox.portal.rest.security.authentication.AnonymouslyAccessible;
import ai.salesfox.portal.rest.security.authentication.DefaultAuthenticationHandlers;
import ai.salesfox.portal.rest.security.authentication.user.PortalUserDetailsService;
import ai.salesfox.portal.rest.security.authorization.AdminOnlyAccessible;
import ai.salesfox.portal.rest.security.authorization.CsrfIgnorable;
import ai.salesfox.portal.rest.security.authorization.PortalAuthorityConstants;
import ai.salesfox.portal.rest.security.common.DefaultAllowedEndpoints;
import ai.salesfox.portal.rest.security.common.SecurityInterface;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

@Component
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class PortalSecurityConfig extends WebSecurityConfigurerAdapter {
    public static final String PORTAL_COOKIE_NAME = "PORTAL_SESSION_ID";

    private final CsrfTokenRepository csrfTokenRepository;
    private final RoleRepository roleRepository;
    private final List<CsrfIgnorable> csrfIgnorables;
    private final List<AnonymouslyAccessible> anonymouslyAccessibles;
    private final List<AdminOnlyAccessible> adminOnlyAccessibles;
    private final PortalUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public PortalSecurityConfig(
            CsrfTokenRepository csrfTokenRepository,
            RoleRepository roleRepository,
            List<CsrfIgnorable> csrfIgnorables,
            List<AnonymouslyAccessible> anonymouslyAccessibles,
            List<AdminOnlyAccessible> adminOnlyAccessibles,
            PortalUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        this.csrfTokenRepository = csrfTokenRepository;
        this.roleRepository = roleRepository;
        this.csrfIgnorables = csrfIgnorables;
        this.anonymouslyAccessibles = anonymouslyAccessibles;
        this.adminOnlyAccessibles = adminOnlyAccessibles;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder authManagerBuilder) throws Exception {
        authManagerBuilder
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder);
    }

    @Override
    protected void configure(HttpSecurity security) throws Exception {
        HttpSecurity sslSecured = configureSSL(security);
        HttpSecurity csrfSecured = configureCsrf(sslSecured);
        HttpSecurity corsAllowed = configureCors(csrfSecured);
        HttpSecurity adminPermissionsSecured = configureAdminPermissions(corsAllowed);
        HttpSecurity defaultPermissionsSecured = configureDefaultPermissions(adminPermissionsSecured);
        HttpSecurity errorHandlingSecured = configureErrorHandling(defaultPermissionsSecured);
        HttpSecurity loginSecured = configureLogin(errorHandlingSecured);
        configureLogout(loginSecured);
    }

    private HttpSecurity configureSSL(HttpSecurity security) throws Exception {
        // Disabled due to deployment strategy
        return security;
    }

    private HttpSecurity configureCors(HttpSecurity security) throws Exception {
        return security.cors()
                .and();
    }

    private HttpSecurity configureCsrf(HttpSecurity security) throws Exception {
        return security.csrf()
                .csrfTokenRepository(csrfTokenRepository)
                .ignoringAntMatchers(collectCsrfIgnorableResources())
                .and();
    }

    private HttpSecurity configureDefaultPermissions(HttpSecurity security) throws Exception {
        return security.authorizeRequests()
                .antMatchers(collectAnonymouslyAccessibleResources())
                .permitAll()
                .anyRequest()
                .hasAnyAuthority(collectUserAuthoritiesFromDatabase())
                .and();
    }

    private HttpSecurity configureAdminPermissions(HttpSecurity security) throws Exception {
        return security.authorizeRequests()
                .antMatchers(collectAdminOnlyResources())
                .hasAuthority(PortalAuthorityConstants.PORTAL_ADMIN)
                .and();
    }

    private HttpSecurity configureErrorHandling(HttpSecurity security) throws Exception {
        return security.exceptionHandling()
                .authenticationEntryPoint(DefaultAuthenticationHandlers.AUTHENTICATION_ENTRY_POINT_HANDLER)
                .accessDeniedHandler(DefaultAuthenticationHandlers.AUTHENTICATION_ACCESS_DENIED_HANDLER)
                .and();
    }

    private HttpSecurity configureLogin(HttpSecurity security) throws Exception {
        return security.formLogin()
                .loginPage(DefaultAllowedEndpoints.LOGIN_ENDPOINT)
                .permitAll()
                .successHandler(DefaultAuthenticationHandlers.AUTHENTICATION_SUCCESS_HANDLER)
                .failureHandler(DefaultAuthenticationHandlers.AUTHENTICATION_FAILURE_HANDLER)
                .and();
    }

    private void configureLogout(HttpSecurity security) throws Exception {
        security.logout()
                .logoutRequestMatcher(new AntPathRequestMatcher(DefaultAllowedEndpoints.LOGOUT_ENDPOINT))
                .deleteCookies(PORTAL_COOKIE_NAME)
                .logoutSuccessHandler(DefaultAuthenticationHandlers.LOGOUT_SUCCESS_HANDLER);
    }

    private String[] collectUserAuthoritiesFromDatabase() {
        return roleRepository.findAll()
                .stream()
                .map(RoleEntity::getRoleLevel)
                .filter(roleLevel -> !roleLevel.startsWith(PortalAuthorityConstants.TEMPORARY_AUTHORITY_PREFIX))
                .toArray(String[]::new);
    }

    private String[] collectCsrfIgnorableResources() {
        return collectFlattenedStrings(csrfIgnorables, CsrfIgnorable::csrfIgnorableApiAntMatchers);
    }

    private String[] collectAnonymouslyAccessibleResources() {
        String[] staticResourceEndpoints = collectFlattenedStrings(anonymouslyAccessibles, AnonymouslyAccessible::anonymouslyAccessibleStaticResourceAntMatchers);
        String[] apiEndpoints = collectFlattenedStrings(anonymouslyAccessibles, AnonymouslyAccessible::anonymouslyAccessibleApiAntMatchers);
        return ArrayUtils.addAll(staticResourceEndpoints, apiEndpoints);
    }

    private String[] collectAdminOnlyResources() {
        String[] staticResourceEndpoints = collectFlattenedStrings(adminOnlyAccessibles, AdminOnlyAccessible::adminOnlyStaticResourceAntMatchers);
        String[] apiEndpoints = collectFlattenedStrings(adminOnlyAccessibles, AdminOnlyAccessible::adminOnlyApiAntMatchers);
        return ArrayUtils.addAll(staticResourceEndpoints, apiEndpoints);
    }

    private <T extends SecurityInterface> String[] collectFlattenedStrings(Collection<T> securityInterfaces, Function<T, String[]> stringExtractor) {
        return collectFlattenedStrings(securityInterfaces, stringExtractor, Function.identity());
    }

    private <T extends SecurityInterface> String[] collectFlattenedStrings(Collection<T> securityInterfaces, Function<T, String[]> stringExtractor, Function<String, String> postProcessor) {
        return securityInterfaces
                .stream()
                .map(stringExtractor)
                .flatMap(Arrays::stream)
                .filter(StringUtils::isNotBlank)
                .map(postProcessor)
                .distinct()
                .toArray(String[]::new);
    }

}
