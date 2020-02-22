package com.usepipeline.portal.web.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Component
public class SecuritySessionService {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private CsrfTokenRepository csrfTokenRepository;

    @Autowired
    public SecuritySessionService(CsrfTokenRepository csrfTokenRepository) {
        this.csrfTokenRepository = csrfTokenRepository;
    }

    public void beginSession(HttpServletRequest request, HttpServletResponse response) {
        logger.debug("Generating a csrf token...");
        CsrfToken token = csrfTokenRepository.generateToken(request);
        csrfTokenRepository.saveToken(token, request, response);
        response.setHeader(token.getHeaderName(), token.getToken());
    }

    public void invalidateSession(HttpServletRequest request, HttpServletResponse response) {
        logger.debug("Invalidating session...");
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        SecurityContextHolder.clearContext();
        response.addHeader("Location", "/");
    }

}
