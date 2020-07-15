package com.usepipeline.portal.database.account.entity;

import com.usepipeline.portal.database.account.key.PasswordResetTokenPK;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@IdClass(PasswordResetTokenPK.class)
@Table(schema = "portal", name = "password_reset_tokens")
public class PasswordResetTokenEntity implements Serializable {
    @Id
    @Column(name = "email")
    private String email;
    @Id
    @Column(name = "token")
    private String token;
    @Column(name = "date_generated")
    private OffsetDateTime dateGenerated;

}
