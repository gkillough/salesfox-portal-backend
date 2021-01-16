package ai.salesfox.portal.task.api;

import ai.salesfox.portal.rest.api.common.InternalEndpointConstants;
import ai.salesfox.portal.rest.security.authentication.AnonymouslyAccessible;
import ai.salesfox.portal.rest.security.authorization.CsrfIgnorable;
import ai.salesfox.portal.rest.security.authorization.PortalAuthorityConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(PortalTaskController.BASE_URL)
public class PortalTaskController implements AnonymouslyAccessible, CsrfIgnorable {
    public static final String BASE_URL = InternalEndpointConstants.BASE_URL + "/tasks";
    private static final String INSECURE_ENDPOINT = BASE_URL + "/*";

    private final PortalTaskEndpointService portalTaskEndpointService;

    @Autowired
    public PortalTaskController(PortalTaskEndpointService portalTaskEndpointService) {
        this.portalTaskEndpointService = portalTaskEndpointService;
    }

    @GetMapping
    @PreAuthorize(PortalAuthorityConstants.PORTAL_ADMIN_AUTH_CHECK)
    public MultiPortalTaskResponseModel getTasks() {
        return portalTaskEndpointService.getTasks();
    }

    @PostMapping("/{taskIdOrKey}")
    public void runTask(@PathVariable String taskIdOrKey, @RequestBody PortalTaskAccessTokenModel requestModel) {
        portalTaskEndpointService.runTask(taskIdOrKey, requestModel);
    }

    @GetMapping("/{taskIdOrKey}/token")
    @PreAuthorize(PortalAuthorityConstants.PORTAL_ADMIN_AUTH_CHECK)
    public PortalTaskAccessTokenModel refreshAccessToken(@PathVariable String taskIdOrKey) {
        return portalTaskEndpointService.generateAccessTokenAndReplaceExisting(taskIdOrKey);
    }

    @Override
    public String[] anonymouslyAccessibleApiAntMatchers() {
        return new String[] {
                INSECURE_ENDPOINT
        };
    }

    @Override
    public String[] csrfIgnorableApiAntMatchers() {
        return new String[] {
                INSECURE_ENDPOINT
        };
    }

}
