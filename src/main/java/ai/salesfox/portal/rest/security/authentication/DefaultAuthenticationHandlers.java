package ai.salesfox.portal.rest.security.authentication;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DefaultAuthenticationHandlers {
    public static final AuthenticationEntryPoint AUTHENTICATION_ENTRY_POINT_HANDLER = (HttpServletRequest request, HttpServletResponse response, AuthenticationException authEx) -> {
        response.sendError(HttpStatus.UNAUTHORIZED.value(), "Please Login");
    };

    public static final AuthenticationSuccessHandler AUTHENTICATION_SUCCESS_HANDLER = (HttpServletRequest request, HttpServletResponse response, Authentication authentication) -> {
        response.getWriter().append("OK");
        response.setStatus(200);
    };

    public static final AuthenticationFailureHandler AUTHENTICATION_FAILURE_HANDLER = (HttpServletRequest request, HttpServletResponse response, AuthenticationException authEx) -> {
        response.sendError(HttpStatus.UNAUTHORIZED.value(), authEx.getMessage());
    };

    public static final AccessDeniedHandler AUTHENTICATION_ACCESS_DENIED_HANDLER = (HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedEx) -> {
        response.sendError(HttpStatus.FORBIDDEN.value(), "Access Denied");
    };

    public static final LogoutSuccessHandler LOGOUT_SUCCESS_HANDLER = new HttpStatusReturningLogoutSuccessHandler();

}
