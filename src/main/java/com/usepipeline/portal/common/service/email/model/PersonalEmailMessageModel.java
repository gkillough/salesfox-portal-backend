package com.usepipeline.portal.common.service.email.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class PersonalEmailMessageModel extends EmailMessageModel {
    private String firstName;
    private String lastName;

    public PersonalEmailMessageModel(List<String> recipients, String subjectLine, String primaryMessage, String firstName, String lastName) {
        super(recipients, subjectLine, primaryMessage);
        this.firstName = firstName;
        this.lastName = lastName;
    }

}
