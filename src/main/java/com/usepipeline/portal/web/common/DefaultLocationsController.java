package com.usepipeline.portal.web.common;

import com.usepipeline.portal.web.security.authentication.AnonymousAccessible;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@RestController
public class DefaultLocationsController implements ErrorController, AnonymousAccessible {
    public static final String ROOT_ENDPOINT = "/";
    public static final String ERROR_ENDPOINT = "/error";
    public static final String ACCESS_DENIED_ENDPOINT = ERROR_ENDPOINT;

    @GetMapping(ROOT_ENDPOINT)
    public String landingPage() {
        // TODO point this to the react home location
        return "Pipeline Portal";
    }

    @RequestMapping(ERROR_ENDPOINT)
    public String errorPage(HttpServletRequest request, HttpServletResponse response) {
        log.debug("Error endpoint reached");
        Object errorStatusCodeObject = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (null != errorStatusCodeObject) {
            int responseStatus = Integer.parseInt(errorStatusCodeObject.toString());
            if (responseStatus < 400) {
                return "Error endpoint";
            }
            String errorMessage = (String) request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
            return String.format("Error: %s. Message: %s", responseStatus, errorMessage);
        }
        response.setHeader("Location: ", "/");
        return "Error endpoint";
    }

    @Override
    public String getErrorPath() {
        return "/error";
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
