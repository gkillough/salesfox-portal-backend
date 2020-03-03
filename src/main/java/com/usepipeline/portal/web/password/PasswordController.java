package com.usepipeline.portal.web.password;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
public class PasswordController {
    public static final String BASE_ENDPOINT = "/password";
    public static final String RESET_ENDPOINT = BASE_ENDPOINT + "/reset";
    public static final String GRANT_UPDATE_PERMISSION_ENDPOINT = RESET_ENDPOINT + "/validate";
    public static final String UPDATE_ENDPOINT = BASE_ENDPOINT + "/update";

    private PasswordService passwordService;

    @Autowired
    public PasswordController(PasswordService passwordService) {
        this.passwordService = passwordService;
    }

    @PostMapping(RESET_ENDPOINT)
    public boolean resetPassword(@RequestBody ResetPasswordModel resetPasswordRequest) {
        return passwordService.sendPasswordResetEmail(resetPasswordRequest);
    }

    @GetMapping(GRANT_UPDATE_PERMISSION_ENDPOINT)
    public boolean grantUpdatePasswordPermission(HttpServletResponse response, @RequestParam("email") String emailRequestParam, @RequestParam("token") String tokenRequestParam) {
        return passwordService.validateToken(response, emailRequestParam, tokenRequestParam);
    }

    @PostMapping(UPDATE_ENDPOINT)
    public boolean updatePassword(HttpServletResponse response, @RequestBody UpdatePasswordModel updatePasswordModel) {
        return passwordService.updatePassword(response, updatePasswordModel);
    }

}
