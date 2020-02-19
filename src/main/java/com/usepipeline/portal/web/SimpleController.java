package com.usepipeline.portal.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SimpleController {


    @GetMapping("/")
    public String home() {
        return "Hello World";
    }
}
