package com.usepipeline.portal.web.login;

import com.usepipeline.portal.web.common.MessageResponseModel;
import com.usepipeline.portal.web.login.model.LoginModel;
import com.usepipeline.portal.web.login.model.RegistrationModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@RestController
public class LoginController {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private LoginActions loginActions;
    private CsrfTokenRepository csrfTokenRepository;

    @Autowired
    public LoginController(LoginActions loginActions, CsrfTokenRepository csrfTokenRepository) {
        this.loginActions = loginActions;
        this.csrfTokenRepository = csrfTokenRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> userLogin(HttpServletRequest request, HttpServletResponse response, @RequestBody LoginModel loginRequest) {
        try {
            if (loginActions.handleLogin(loginRequest.getUsername(), loginRequest.getPassword())) {
                CsrfToken token = csrfTokenRepository.generateToken(request);
                csrfTokenRepository.saveToken(token, request, response);
                response.setHeader(token.getHeaderName(), token.getToken());
                return ResponseEntity.noContent().build();
            }
        } catch (final Exception e) {
            logger.error("Login error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @GetMapping("/logout")
    public ResponseEntity<MessageResponseModel> userLogout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", "/");
        return ResponseEntity.status(HttpStatus.NO_CONTENT).headers(headers).build();
    }

    @GetMapping("/reset_password")
    public ResponseEntity<?> userResetPassword() {
        // TODO implement
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }

    @GetMapping("/register")
    public ResponseEntity<?> userRegister(@RequestParam RegistrationModel registrationRequest) {
        // TODO implement
        return null;
    }

}
