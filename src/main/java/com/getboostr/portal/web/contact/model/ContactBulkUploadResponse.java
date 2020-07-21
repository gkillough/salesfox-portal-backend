package com.getboostr.portal.web.contact.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactBulkUploadResponse {
    private Integer requestedUploadCount;
    private Integer successfulUploadCount;
    private List<ContactBulkUploadFieldStatus> fieldStatuses;

}
