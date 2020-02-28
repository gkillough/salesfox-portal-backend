package com.usepipeline.portal.web.security;

import com.usepipeline.portal.web.common.DefaultLocationsController;
import com.usepipeline.portal.web.password.PasswordController;
import com.usepipeline.portal.web.password.PasswordService;
import com.usepipeline.portal.web.registration.RegistrationController;
import com.usepipeline.portal.web.security.authentication.PortalUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;

@Component
@EnableWebSecurity
public class PortalSecurityConfig extends WebSecurityConfigurerAdapter {
    public static final String PORTAL_COOKIE_NAME = "PORTAL_SESSION_ID";

    public static final String LOGIN_ENDPOINT = "/login";
    public static final String LOGOUT_ENDPOINT = "/logout";

    private CsrfTokenRepository csrfTokenRepository;
    private PortalUserDetailsService userDetailsService;
    private PasswordEncoder passwordEncoder;

    private static String withSubDirectories(String baseDirectory) {
        return baseDirectory + "/**";
    }

    @Autowired
    public PortalSecurityConfig(CsrfTokenRepository csrfTokenRepository, PortalUserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        this.csrfTokenRepository = csrfTokenRepository;
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
        HttpSecurity passwordResetSecured = configurePasswordReset(csrfSecured);
        HttpSecurity defaultPermissionsSecured = configureDefaultPermissions(passwordResetSecured);
        HttpSecurity errorHandlingSecured = configureErrorHandling(defaultPermissionsSecured);
        HttpSecurity loginSecured = configureLogin(errorHandlingSecured);
        configureLogout(loginSecured);

        // TODO determine if endpoint based authorization should be used
        //  .antMatchers(baseAndSubDirectories("/admin")).hasRole(PortalRole.PIPELINE_ADMIN.name())
        //  .antMatchers(baseAndSubDirectories("/organization")).hasRole(PortalRole.ORGANIZATION_ACCOUNT_MANAGER.name())
        //  .antMatchers(baseAndSubDirectories("/manager")).hasRole(PortalRole.ORGANIZATION_SALES_REP_MANAGER.name())
        //  .antMatchers(baseAndSubDirectories("/portal")).hasAnyRole(PortalRole.ORGANIZATION_SALES_REP_MANAGER.name(), PortalRole.ORGANIZATION_SALES_REP.name())
    }

    private HttpSecurity configureCsrf(HttpSecurity security) throws Exception {
        return security.csrf()
                .csrfTokenRepository(csrfTokenRepository)
                .ignoringAntMatchers(createCsrfIgnoreAntMatchers())
                .and();
    }

    private HttpSecurity configurePasswordReset(HttpSecurity security) throws Exception {
        return security.authorizeRequests()
                .antMatchers(PasswordController.UPDATE_ENDPOINT)
                .hasAuthority(PasswordService.UPDATE_PASSWORD_AUTHORITY_NAME)
                .and();
    }

    private HttpSecurity configureDefaultPermissions(HttpSecurity security) throws Exception {
        return security.authorizeRequests()
                .antMatchers(createDefaultAllowedEndpoints())
                .permitAll()
                .anyRequest()
                .authenticated()
                .and();
    }

    private HttpSecurity configureErrorHandling(HttpSecurity security) throws Exception {
        return security.exceptionHandling()
                .accessDeniedPage(DefaultLocationsController.ACCESS_DENIED_ENDPOINT)
                .and();
    }

    private HttpSecurity configureLogin(HttpSecurity security) throws Exception {
        return security.formLogin()
                // TODO .successForwardUrl("/portal")
                .and();
    }

    private void configureLogout(HttpSecurity security) throws Exception {
        security.logout()
                .logoutRequestMatcher(new AntPathRequestMatcher(LOGOUT_ENDPOINT))
                .deleteCookies(PORTAL_COOKIE_NAME) // TODO we may not need this cookie with JSESSIONID
                .logoutSuccessUrl(DefaultLocationsController.ROOT_ENDPOINT);
    }

    private String[] createCsrfIgnoreAntMatchers() {
        return new String[]{
                withSubDirectories(RegistrationController.BASE_ENDPOINT),
                PasswordController.RESET_ENDPOINT
        };
    }

    private String[] createDefaultAllowedEndpoints() {
        return new String[]{
                DefaultLocationsController.ROOT_ENDPOINT,
                DefaultLocationsController.ERROR_ENDPOINT,
                DefaultLocationsController.ACCESS_DENIED_ENDPOINT,
                LOGIN_ENDPOINT,
                LOGOUT_ENDPOINT,
                "static/css/**",
                RegistrationController.BASE_ENDPOINT,
                withSubDirectories(RegistrationController.BASE_ENDPOINT),
                PasswordController.RESET_ENDPOINT,
                PasswordController.GRANT_UPDATE_PERMISSION_ENDPOINT
        };
    }

}
