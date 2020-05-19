package com.usepipeline.portal.web.common.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidationResponseModel {
    private Boolean isValid;
    private String message;

    public static ValidationResponseModel valid() {
        return new ValidationResponseModel(true, "Valid");
    }

    public static ValidationResponseModel invalid(String message) {
        return new ValidationResponseModel(false, message);
    }

}
