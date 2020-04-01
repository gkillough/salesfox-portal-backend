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

}
