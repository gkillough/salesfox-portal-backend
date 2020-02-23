package com.usepipeline.portal.web.registration.user;

import lombok.Data;

@Data
public class UserRegistrationModel {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String planType;

}
