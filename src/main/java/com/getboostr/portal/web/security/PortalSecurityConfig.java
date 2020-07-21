package com.getboostr.portal.web.security;

import com.getboostr.portal.database.account.entity.RoleEntity;
import com.getboostr.portal.database.account.repository.RoleRepository;
import com.getboostr.portal.web.registration.RegistrationController;
import com.getboostr.portal.web.security.authentication.AnonymouslyAccessible;
import com.getboostr.portal.web.security.authentication.DefaultAuthenticationHandlers;
import com.getboostr.portal.web.security.authorization.AdminOnlyAccessible;
import com.getboostr.portal.web.security.authorization.CsrfIgnorable;
import com.getboostr.portal.web.security.authorization.PortalAuthorityConstants;
import com.getboostr.portal.web.security.common.DefaultAllowedEndpoints;
import com.getboostr.portal.web.security.common.SecurityInterface;
import com.getboostr.portal.web.password.PasswordController;
import com.getboostr.portal.web.security.authentication.user.PortalUserDetailsService;
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
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

@Component
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class PortalSecurityConfig extends WebSecurityConfigurerAdapter {
    public static final String PORTAL_COOKIE_NAME = "PORTAL_SESSION_ID";

    private CsrfTokenRepository csrfTokenRepository;
    private RoleRepository roleRepository;
    private List<CsrfIgnorable> csrfIgnorables;
    private List<AnonymouslyAccessible> anonymouslyAccessibles;
    private List<AdminOnlyAccessible> adminOnlyAccessibles;
    private PortalUserDetailsService userDetailsService;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public PortalSecurityConfig(CsrfTokenRepository csrfTokenRepository, RoleRepository roleRepository,
                                List<CsrfIgnorable> csrfIgnorables, List<AnonymouslyAccessible> anonymouslyAccessibles, List<AdminOnlyAccessible> adminOnlyAccessibles,
                                PortalUserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
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
        HttpSecurity csrfSecured = configureCsrf(security);
        HttpSecurity corsAllowed = configureCors(csrfSecured);
        HttpSecurity passwordResetSecured = configurePasswordReset(corsAllowed);
        HttpSecurity orgAccountRegistrationSecured = configureOrganizationAccountRegistration(passwordResetSecured);
        HttpSecurity adminPermissionsSecured = configureAdminPermissions(orgAccountRegistrationSecured);
        HttpSecurity defaultPermissionsSecured = configureDefaultPermissions(adminPermissionsSecured);
        HttpSecurity errorHandlingSecured = configureErrorHandling(defaultPermissionsSecured);
        HttpSecurity loginSecured = configureLogin(errorHandlingSecured);
        configureLogout(loginSecured);
    }

    private HttpSecurity configureCors(HttpSecurity security) throws Exception {
        CorsConfiguration corsConfiguration = new CorsConfiguration().applyPermitDefaultValues();
        corsConfiguration.setAllowCredentials(Boolean.TRUE);
        return security.cors()
                .configurationSource(request -> corsConfiguration)
                .and();
    }

    private HttpSecurity configureCsrf(HttpSecurity security) throws Exception {
        String[] ignoredAntMatchers = collectFlattenedStrings(csrfIgnorables, CsrfIgnorable::ignoredEndpointAntMatchers);
        return security.csrf()
                .csrfTokenRepository(csrfTokenRepository)
                .ignoringAntMatchers(ignoredAntMatchers)
                .and();
    }

    // TODO consider an interface for password reset and org account registration to implement, then use it here instead of having two similar methods
    private HttpSecurity configurePasswordReset(HttpSecurity security) throws Exception {
        return security.authorizeRequests()
                .antMatchers(PasswordController.UPDATE_ENDPOINT)
                .hasAuthority(PortalAuthorityConstants.UPDATE_PASSWORD_PERMISSION)
                .and();
    }

    private HttpSecurity configureOrganizationAccountRegistration(HttpSecurity security) throws Exception {
        return security.authorizeRequests()
                .antMatchers(RegistrationController.BASE_ENDPOINT + RegistrationController.ORGANIZATION_ACCOUNT_USER_ENDPOINT_SUFFIX)
                .hasAuthority(PortalAuthorityConstants.CREATE_ORGANIZATION_ACCOUNT_PERMISSION)
                .and();
    }

    private HttpSecurity configureDefaultPermissions(HttpSecurity security) throws Exception {
        return security.authorizeRequests()
                .antMatchers(collectAnonymouslyAccessibleEndpoints())
                .permitAll()
                .anyRequest()
                .hasAnyAuthority(collectUserAuthoritiesFromDatabase())
                .and();
    }

    private HttpSecurity configureAdminPermissions(HttpSecurity security) throws Exception {
        return security.authorizeRequests()
                .antMatchers(collectAdminOnlyEndpoints())
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

    private String[] collectAnonymouslyAccessibleEndpoints() {
        return collectFlattenedStrings(anonymouslyAccessibles, AnonymouslyAccessible::allowedEndpointAntMatchers);
    }

    private String[] collectAdminOnlyEndpoints() {
        return collectFlattenedStrings(adminOnlyAccessibles, AdminOnlyAccessible::adminOnlyEndpointAntMatchers);
    }

    private <T extends SecurityInterface> String[] collectFlattenedStrings(Collection<T> securityInterfaces, Function<T, String[]> stringExtractor) {
        return securityInterfaces
                .stream()
                .map(stringExtractor)
                .flatMap(Arrays::stream)
                .distinct()
                .toArray(String[]::new);
    }

}
