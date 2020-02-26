package com.usepipeline.portal.web.password;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.websocket.server.PathParam;

@RestController
@RequestMapping(PasswordController.BASE_ENDPOINT)
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
    public boolean grantUpdatePasswordPermission(HttpServletResponse response, @PathParam("email") String emailRequestParam, @PathParam("token") String tokenRequestParam) {
        return passwordService.validateToken(response, emailRequestParam, tokenRequestParam);
    }

    @PostMapping(UPDATE_ENDPOINT)
    public boolean updatePassword(@RequestBody UpdatePasswordModel updatePasswordModel) {
        return passwordService.updatePassword(updatePasswordModel);
    }

}
