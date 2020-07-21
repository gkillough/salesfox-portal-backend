package com.getboostr.portal.web.contact.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ContactBulkUploadFieldStatus {
    private Integer fieldNumber;
    private Boolean hasError;
    private String errorMessage;

    public ContactBulkUploadFieldStatus(Integer fieldNumber, String errorMessage) {
        this.fieldNumber = fieldNumber;
        this.hasError = errorMessage != null;
        this.errorMessage = errorMessage;
    }

}
