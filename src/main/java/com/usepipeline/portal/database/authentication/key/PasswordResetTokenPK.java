package com.usepipeline.portal.database.authentication.key;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetTokenPK implements Serializable {
    private String email;
    private String token;

}
