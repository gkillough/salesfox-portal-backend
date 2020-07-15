package com.usepipeline.portal.database.account.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(schema = "portal", name = "logins")
public class LoginEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "login_id")
    private UUID loginId;

    @PrimaryKeyJoinColumn
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "last_successful_login")
    private OffsetDateTime lastSuccessfulLogin;

    @Column(name = "last_locked")
    private OffsetDateTime lastLocked;

    @Column(name = "num_failed_logins")
    private Integer numFailedLogins;

}
