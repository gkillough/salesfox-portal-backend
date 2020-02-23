package com.usepipeline.portal.web.security;

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
    private AuthenticationUserDetailsService userDetailsService;
    private PasswordEncoder passwordEncoder;
    private CsrfTokenRepository csrfTokenRepository;

    @Autowired
    public PortalSecurityConfig(AuthenticationUserDetailsService userDetailsService, PasswordEncoder passwordEncoder, CsrfTokenRepository csrfTokenRepository) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.csrfTokenRepository = csrfTokenRepository;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder authManagerBuilder) throws Exception {
        authManagerBuilder
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder);
    }

    @Override
    protected void configure(HttpSecurity security) throws Exception {
        // TODO extract strings
        security.authorizeRequests()
                .antMatchers("/", "/login", "/error", "static/css/**").permitAll()
                .antMatchers("/admin", "/admin/**").hasRole("PIPELINE_ADMIN")
                .antMatchers("/manager", "/manager/**").hasRole("PIPELINE_MANAGER")
                .antMatchers("/portal", "/portal/**").hasAnyRole("PIPELINE_MANAGER", "PIPELINE_USER")
                .and()
                .logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .deleteCookies("PORTAL_SESSION_ID")
                .logoutSuccessUrl("/");
    }

}
