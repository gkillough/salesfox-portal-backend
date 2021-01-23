package ai.salesfox.portal.rest.api.audit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

@RestController
public class ExposedEndpointsController {
    public final RequestMappingHandlerMapping handlerMapping;

    @Autowired
    public ExposedEndpointsController(RequestMappingHandlerMapping handlerMapping) {
        this.handlerMapping = handlerMapping;
    }

    @GetMapping("/auditEndpoints")
    public Map<String, Set<RequestMethod>> get() {
        Map<String, Set<RequestMethod>> restMappings = new TreeMap<>();
        for (RequestMappingInfo info : handlerMapping.getHandlerMethods().keySet()) {
            for (String apiPath : info.getPatternsCondition().getPatterns()) {
                if (apiPath != null) {
                    if (!restMappings.containsKey(apiPath)) {
                        restMappings.put(apiPath, new HashSet<>());
                    }
                    restMappings.get(apiPath).addAll(info.getMethodsCondition().getMethods());
                }
            }
        }
        return restMappings;
    }

}