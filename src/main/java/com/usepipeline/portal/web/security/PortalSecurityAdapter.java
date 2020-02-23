package com.usepipeline.portal.web.security;

import com.usepipeline.portal.web.security.authentication.AuthenticationUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.stereotype.Component;

@Component
@EnableWebSecurity
public class PortalSecurityAdapter extends WebSecurityConfigurerAdapter {
    private AuthenticationUserDetailsService userDetailsService;
    private CsrfTokenRepository csrfTokenRepository;

    @Autowired
    public PortalSecurityAdapter(AuthenticationUserDetailsService userDetailsService, CsrfTokenRepository csrfTokenRepository) {
        this.userDetailsService = userDetailsService;
        this.csrfTokenRepository = csrfTokenRepository;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService);
    }

    @Override
    protected void configure(HttpSecurity security) throws Exception {
        // TODO extract strings
        security.authorizeRequests()
                .antMatchers("/", "/error", "static/css/**").permitAll()
                .antMatchers("/admin", "/admin/**").hasRole("PIPELINE_ADMIN")
                .antMatchers("/manager", "/manager/**").hasRole("PIPELINE_MANAGER")
                .antMatchers("/portal", "/portal/**").hasAnyRole("PIPELINE_MANAGER", "PIPELINE_USER")
                .and()
                .formLogin().disable()
                .logout().logoutSuccessUrl("/");
    }

}
