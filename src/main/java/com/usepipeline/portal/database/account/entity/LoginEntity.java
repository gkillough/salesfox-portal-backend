package com.usepipeline.portal.database.account.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(schema = "portal", name = "logins")
public class LoginEntity implements Serializable {
    @Id
    @SequenceGenerator(schema = "portal", name = "logins_login_id_seq_generator", sequenceName = "logins_login_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "logins_login_id_seq_generator")
    @Column(name = "login_id")
    private Long loginId;

    @PrimaryKeyJoinColumn
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "last_successful_login")
    private LocalDateTime lastSuccessfulLogin;

    @Column(name = "last_locked")
    private LocalDateTime lastLocked;

    @Column(name = "num_failed_logins")
    private Integer numFailedLogins;

}
