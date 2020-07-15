package com.usepipeline.portal.web.user.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginInfoModel {
    private OffsetDateTime lastSuccessfulLogin;
    private OffsetDateTime lastLocked;
    private Boolean isLocked;

}
