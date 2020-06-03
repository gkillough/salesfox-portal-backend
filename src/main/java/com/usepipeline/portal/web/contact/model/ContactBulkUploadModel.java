package com.usepipeline.portal.web.contact.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactBulkUploadModel {
    private List<ContactUploadModel> contacts;

}
