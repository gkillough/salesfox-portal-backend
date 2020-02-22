package com.usepipeline.portal.web.security;

import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.access.expression.WebExpressionVoter;
import org.springframework.security.web.csrf.CsrfTokenRepository;

public class PortalSecurityAdapter extends WebSecurityConfigurerAdapter {
    private CsrfTokenRepository csrfTokenRepository;

    @Override
    protected void configure(HttpSecurity security) throws Exception {
        // TODO add security configurations
        security
                .formLogin().disable()
                .httpBasic().disable()
                .authorizeRequests()
                .antMatchers("/").permitAll()
                .and()
                .formLogin()
                .and().csrf().csrfTokenRepository(csrfTokenRepository);
//                .authorizeRequests().withObjectPostProcessor(createRoleProcessor());
    }

    private ObjectPostProcessor<AffirmativeBased> createRoleProcessor() {
        return new ObjectPostProcessor<AffirmativeBased>() {
            @Override
            public AffirmativeBased postProcess(AffirmativeBased affirmativeBased) {
                WebExpressionVoter webExpressionVoter = new WebExpressionVoter();
                DefaultWebSecurityExpressionHandler expressionHandler = new DefaultWebSecurityExpressionHandler();
                expressionHandler.setRoleHierarchy(authorities -> {
                    String[] allowedRoles = retrieveAllowedRoles();
                    return AuthorityUtils.createAuthorityList(allowedRoles);
                });
                webExpressionVoter.setExpressionHandler(expressionHandler);
                affirmativeBased.getDecisionVoters().add(webExpressionVoter);
                return affirmativeBased;
            }
        };
    }

    private String[] retrieveAllowedRoles() {
        // TODO implement
        return new String[]{"PIPELINE_ADMIN", "PIPELINE_MANAGER"};
    }

}
