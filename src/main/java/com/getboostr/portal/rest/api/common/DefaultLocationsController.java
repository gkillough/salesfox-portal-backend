package com.getboostr.portal.rest.api.common;

import com.getboostr.portal.rest.security.authentication.AnonymouslyAccessible;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class DefaultLocationsController implements AnonymouslyAccessible {
    public static final String ROOT_ENDPOINT = "/";
    public static final String ERROR_ENDPOINT = "/error";
    public static final String ACCESS_DENIED_ENDPOINT = ERROR_ENDPOINT;

    @GetMapping(ROOT_ENDPOINT)
    public String landingPage() {
        // TODO point this to the react home location
        return "Pipeline Portal";
    }

    @Override
    public String[] allowedEndpointAntMatchers() {
        return new String[]{
                DefaultLocationsController.ROOT_ENDPOINT,
                DefaultLocationsController.ERROR_ENDPOINT,
                DefaultLocationsController.ACCESS_DENIED_ENDPOINT
        };
    }

}
