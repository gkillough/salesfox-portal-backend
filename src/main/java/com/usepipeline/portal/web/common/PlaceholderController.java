package com.usepipeline.portal.web.common;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class PlaceholderController implements ErrorController {
    @GetMapping("/")
    public String landingPage() {
        return "Hello";
    }

    @RequestMapping("/error")
    public String errorPage(HttpServletRequest request, HttpServletResponse response) {
        Object errorStatusCodeObject = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (null != errorStatusCodeObject) {
            int responseStatus = Integer.parseInt(errorStatusCodeObject.toString());
            if (responseStatus < 400) {
                return "Error endpoint";
            }
            return "Error: " + responseStatus;
        }
        response.setHeader("Location: ", "/");
        return "Error endpoint";
    }

    @Override
    public String getErrorPath() {
        return "/error";
    }

}
