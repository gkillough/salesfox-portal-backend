package com.usepipeline.portal.web.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidationModel {
    private Boolean isValid;
    private String message;

    public static ValidationModel valid() {
        return new ValidationModel(true, "Valid");
    }

    public static ValidationModel invalid(String message) {
        return new ValidationModel(false, message);
    }

}
