package ai.salesfox.portal.rest.api.password;

import ai.salesfox.portal.rest.security.authentication.AnonymouslyAccessible;
import ai.salesfox.portal.rest.security.authorization.CsrfIgnorable;
import ai.salesfox.portal.rest.security.authorization.PortalAuthorityConstants;
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

    private final PasswordService passwordService;

    @Autowired
    public PasswordController(PasswordService passwordService) {
        this.passwordService = passwordService;
    }

    @PostMapping(RESET_ENDPOINT)
    public void resetPassword(@RequestBody ResetPasswordModel resetPasswordRequest) {
        passwordService.sendPasswordResetEmail(resetPasswordRequest);
    }

    @GetMapping(GRANT_UPDATE_PERMISSION_ENDPOINT)
    public void grantUpdatePasswordPermission(HttpServletResponse response, @RequestParam("email") String emailRequestParam, @RequestParam("token") String tokenRequestParam) {
        passwordService.validateToken(response, emailRequestParam, tokenRequestParam);
    }

    @PostMapping(UPDATE_ENDPOINT)
    @PreAuthorize(PortalAuthorityConstants.UPDATE_PASSWORD_PERMISSION_AUTH_CHECK)
    public boolean updatePassword(HttpServletResponse response, @RequestBody UpdatePasswordModel updatePasswordModel) {
        return passwordService.updateAuthenticatedUserPassword(response, updatePasswordModel);
    }

    @Override
    public String[] anonymouslyAccessibleApiAntMatchers() {
        return new String[] {
                PasswordController.RESET_ENDPOINT,
                PasswordController.GRANT_UPDATE_PERMISSION_ENDPOINT
        };
    }

    @Override
    public String[] csrfIgnorableApiAntMatchers() {
        return new String[] {
                PasswordController.RESET_ENDPOINT
        };
    }

}
