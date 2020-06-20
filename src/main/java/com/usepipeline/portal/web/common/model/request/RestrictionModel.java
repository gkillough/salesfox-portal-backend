package com.usepipeline.portal.web.common.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestrictionModel {
    private UUID organizationAccountId;
    private UUID userId;

}
