package com.getboostr.portal.common.service.email.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ButtonEmailMessageModel extends EmailMessageModel {
    private String buttonLabel;
    private String buttonLink;

    public ButtonEmailMessageModel(List<String> recipients, String subjectLine, String messageTitle, String primaryMessage, String buttonLabel, String buttonLink) {
        super(recipients, subjectLine, messageTitle, primaryMessage);
        this.buttonLabel = buttonLabel;
        this.buttonLink = buttonLink;
    }

}
