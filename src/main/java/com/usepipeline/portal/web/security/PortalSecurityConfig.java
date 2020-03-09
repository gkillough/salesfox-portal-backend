package com.usepipeline.portal.web.security;

import com.usepipeline.portal.web.common.DefaultLocationsController;
import com.usepipeline.portal.web.password.PasswordController;
import com.usepipeline.portal.web.security.authentication.AnonymousAccessible;
import com.usepipeline.portal.web.security.authentication.user.PortalUserDetailsService;
import com.usepipeline.portal.web.security.authorization.CsrfIgnorable;
import com.usepipeline.portal.web.security.authorization.PortalAuthorityConstants;
import com.usepipeline.portal.web.security.common.DefaultAllowedEndpoints;
import com.usepipeline.portal.web.security.common.SecurityInterface;
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

    private CsrfTokenRepository csrfTokenRepository;
    private List<CsrfIgnorable> csrfIgnorables;
    private List<AnonymousAccessible> anonymousAccessibles;
    private PortalUserDetailsService userDetailsService;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public PortalSecurityConfig(CsrfTokenRepository csrfTokenRepository, List<CsrfIgnorable> csrfIgnorables,
                                List<AnonymousAccessible> anonymousAccessibles,
                                PortalUserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        this.csrfTokenRepository = csrfTokenRepository;
        this.csrfIgnorables = csrfIgnorables;
        this.anonymousAccessibles = anonymousAccessibles;
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
    }

    private HttpSecurity configureCsrf(HttpSecurity security) throws Exception {
        String[] ignoredAntMatchers = collectFlattenedStrings(csrfIgnorables, CsrfIgnorable::ignoredEndpointAntMatchers);
        return security.csrf()
                .csrfTokenRepository(csrfTokenRepository)
                .ignoringAntMatchers(ignoredAntMatchers)
                .and();
    }

    private HttpSecurity configurePasswordReset(HttpSecurity security) throws Exception {
        return security.authorizeRequests()
                .antMatchers(PasswordController.UPDATE_ENDPOINT)
                .hasAuthority(PortalAuthorityConstants.UPDATE_PASSWORD_PERMISSION)
                .and();
    }

    private HttpSecurity configureDefaultPermissions(HttpSecurity security) throws Exception {
        return security.authorizeRequests()
                .antMatchers(createAllowedEndpoints())
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
                .and();
    }

    private void configureLogout(HttpSecurity security) throws Exception {
        security.logout()
                .logoutRequestMatcher(new AntPathRequestMatcher(DefaultAllowedEndpoints.LOGOUT_ENDPOINT))
                .deleteCookies(PORTAL_COOKIE_NAME) // TODO we may not need this cookie with JSESSIONID
                .logoutSuccessUrl(DefaultLocationsController.ROOT_ENDPOINT);
    }

    private String[] createAllowedEndpoints() {
        return collectFlattenedStrings(anonymousAccessibles, AnonymousAccessible::allowedEndpointAntMatchers);
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
