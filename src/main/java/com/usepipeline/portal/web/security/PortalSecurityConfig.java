package com.usepipeline.portal.web.security;

import com.usepipeline.portal.web.common.DefaultLocationsController;
import com.usepipeline.portal.web.registration.RegistrationController;
import com.usepipeline.portal.web.security.authentication.AuthenticationUserDetailsService;
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

    public static final String[] DEFAULT_ALLOWED_ENDPOINTS = {
            DefaultLocationsController.ROOT_ENDPOINT,
            DefaultLocationsController.ERROR_ENDPOINT,
            DefaultLocationsController.ACCESS_DENIED_ENDPOINT,
            LOGIN_ENDPOINT,
            LOGOUT_ENDPOINT,
            "static/css/**",
            RegistrationController.BASE_ENDPOINT,
            withSubDirectories(RegistrationController.BASE_ENDPOINT)
    };

    private CsrfTokenRepository csrfTokenRepository;
    private AuthenticationUserDetailsService userDetailsService;
    private PasswordEncoder passwordEncoder;

    private static String withSubDirectories(String baseDirectory) {
        return baseDirectory + "/**";
    }


    @Autowired
    public PortalSecurityConfig(CsrfTokenRepository csrfTokenRepository, AuthenticationUserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
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
        security
                .csrf()
                .csrfTokenRepository(csrfTokenRepository)
                .ignoringAntMatchers(withSubDirectories(RegistrationController.BASE_ENDPOINT))
                .and()
                .authorizeRequests()
                .antMatchers(DEFAULT_ALLOWED_ENDPOINTS).permitAll()
                // TODO determine if endpoint based authorization should be used
//                .antMatchers(baseAndSubDirectories("/admin")).hasRole(PortalRole.PIPELINE_ADMIN.name())
//                .antMatchers(baseAndSubDirectories("/organization")).hasRole(PortalRole.ORGANIZATION_ACCOUNT_MANAGER.name())
//                .antMatchers(baseAndSubDirectories("/manager")).hasRole(PortalRole.ORGANIZATION_SALES_REP_MANAGER.name())
//                .antMatchers(baseAndSubDirectories("/portal")).hasAnyRole(PortalRole.ORGANIZATION_SALES_REP_MANAGER.name(), PortalRole.ORGANIZATION_SALES_REP.name())
                .and()
                .exceptionHandling().accessDeniedPage(DefaultLocationsController.ACCESS_DENIED_ENDPOINT)
                .and()
                .formLogin().successForwardUrl("/portal")
                .and()
                .logout()
                .logoutRequestMatcher(new AntPathRequestMatcher(LOGOUT_ENDPOINT))
                .deleteCookies(PORTAL_COOKIE_NAME)
                .logoutSuccessUrl(DefaultLocationsController.ROOT_ENDPOINT);
    }

}
