package com.usepipeline.portal.web.login;

import com.usepipeline.portal.web.login.model.LoginModel;
import com.usepipeline.portal.web.login.model.RegistrationModel;
import com.usepipeline.portal.web.security.SecuritySessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class LoginController {
    private SecuritySessionService securitySessionService;
    private UserLoginService userLoginService;

    @Autowired
    public LoginController(SecuritySessionService securitySessionService, UserLoginService userLoginService) {
        this.securitySessionService = securitySessionService;
        this.userLoginService = userLoginService;
    }

    @PostMapping("/login")
    public Boolean userLogin(HttpServletRequest request, HttpServletResponse response, @RequestBody LoginModel loginRequest) {
        if (userLoginService.handleLogin(loginRequest.getUsername(), loginRequest.getPassword())) {
            securitySessionService.beginSession(request, response);
            return true;
        }
        return false;
    }

    @GetMapping("/logout")
    public Boolean userLogout(HttpServletRequest request, HttpServletResponse response) {
        securitySessionService.invalidateSession(request, response);
        return true;
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
