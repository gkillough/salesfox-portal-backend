package com.getboostr.portal.web.password;

import com.getboostr.portal.web.security.authentication.AnonymouslyAccessible;
import com.getboostr.portal.web.security.authorization.CsrfIgnorable;
import com.getboostr.portal.web.security.authorization.PortalAuthorityConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
public class PasswordController implements CsrfIgnorable, AnonymouslyAccessible {
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
    @PreAuthorize(PortalAuthorityConstants.UPDATE_PASSWORD_PERMISSION_AUTH_CHECK)
    public boolean updatePassword(HttpServletResponse response, @RequestBody UpdatePasswordModel updatePasswordModel) {
        return passwordService.updateAuthenticatedUserPassword(response, updatePasswordModel);
    }

    @Override
    public String[] ignoredEndpointAntMatchers() {
        return new String[]{
                PasswordController.RESET_ENDPOINT,
                PasswordController.UPDATE_ENDPOINT
        };
    }

    @Override
    public String[] allowedEndpointAntMatchers() {
        return new String[]{
                PasswordController.RESET_ENDPOINT,
                PasswordController.GRANT_UPDATE_PERMISSION_ENDPOINT
        };
    }

}
