package com.usepipeline.portal.web.user.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginInfoModel {
    private LocalDateTime lastSuccessfulLogin;
    private LocalDateTime lastLocked;
    private Boolean isLocked;

}
