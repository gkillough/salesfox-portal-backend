package ai.salesfox.portal.rest.api.password;

import ai.salesfox.portal.rest.api.common.model.response.ValidationResponseModel;
import ai.salesfox.portal.rest.api.password.model.ResetPasswordModel;
import ai.salesfox.portal.rest.api.password.model.UpdatePasswordModel;
import ai.salesfox.portal.rest.security.authentication.AnonymouslyAccessible;
import ai.salesfox.portal.rest.security.authorization.CsrfIgnorable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@RequestBody ResetPasswordModel resetPasswordRequest) {
        passwordService.sendPasswordResetEmail(resetPasswordRequest);
    }

    // TODO consider making this a POST endpoint
    @GetMapping(GRANT_UPDATE_PERMISSION_ENDPOINT)
    public ValidationResponseModel grantUpdatePasswordPermission(@RequestParam("email") String emailRequestParam, @RequestParam("token") String tokenRequestParam) {
        return passwordService.validateToken(emailRequestParam, tokenRequestParam);
    }

    @PostMapping(UPDATE_ENDPOINT)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updatePassword(@RequestBody UpdatePasswordModel updatePasswordModel) {
        passwordService.updatePasswordWithTokenAndEmail(updatePasswordModel);
    }

    @Override
    public String[] anonymouslyAccessibleApiAntMatchers() {
        return new String[] {
                PasswordController.RESET_ENDPOINT,
                PasswordController.GRANT_UPDATE_PERMISSION_ENDPOINT,
                PasswordController.UPDATE_ENDPOINT
        };
    }

    @Override
    public String[] csrfIgnorableApiAntMatchers() {
        return new String[] {
                PasswordController.RESET_ENDPOINT,
                PasswordController.UPDATE_ENDPOINT
        };
    }

}
