package com.usepipeline.portal.web.user.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoggedInUserModel {
    private String firstName;
    private String lastName;
    private String email;

    public static final LoggedInUserModel ANONYMOUS_USER = new LoggedInUserModel("Anonymous", "Anonymous", "None");

}
