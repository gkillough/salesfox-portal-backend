package ai.salesfox.portal.task.api;

import ai.salesfox.portal.rest.api.common.InternalEndpointConstants;
import ai.salesfox.portal.rest.security.authentication.AnonymouslyAccessible;
import ai.salesfox.portal.rest.security.authorization.PortalAuthorityConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(PortalTaskController.BASE_URL)
public class PortalTaskController implements AnonymouslyAccessible {
    public static final String BASE_URL = InternalEndpointConstants.BASE_URL + "/task";

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
    public void runTask(String taskIdOrKey, @RequestParam String token) {
        portalTaskEndpointService.runTask(taskIdOrKey, token);
    }

    // TODO add access token endpoint(s)

    @Override
    public String[] anonymouslyAccessibleApiAntMatchers() {
        return new String[] {
                BASE_URL + "/*"
        };
    }

}
