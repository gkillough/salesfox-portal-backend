package com.getboostr.portal.web.security.authorization;

import com.getboostr.portal.web.security.authentication.AnonymouslyAccessible;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class CsrfTokenProviderController implements AnonymouslyAccessible {
    public static final String CSRF_TOKEN_REQUEST_ENDPOINT = "/csrf/token";
    private CsrfTokenRepository csrfTokenRepository;

    @Autowired
    public CsrfTokenProviderController(CsrfTokenRepository csrfTokenRepository) {
        this.csrfTokenRepository = csrfTokenRepository;
    }

    @GetMapping(CSRF_TOKEN_REQUEST_ENDPOINT)
    public void getToken(HttpServletRequest request, HttpServletResponse response) {
        CsrfToken csrfToken = csrfTokenRepository.loadToken(request);
        response.setHeader(csrfToken.getHeaderName(), csrfToken.getToken());
    }

    @Override
    public String[] allowedEndpointAntMatchers() {
        return new String[]{
                CSRF_TOKEN_REQUEST_ENDPOINT
        };
    }

}
