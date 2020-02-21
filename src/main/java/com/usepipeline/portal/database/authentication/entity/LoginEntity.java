package com.usepipeline.portal.database.authentication.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(schema = "portal", name = "logins")
public class LoginEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "login_id")
    private Long loginId;

    @PrimaryKeyJoinColumn
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "password_salt")
    private String passwordSalt;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "last_successful_login")
    private Date lastSuccessfulLogin;

    @Column(name = "num_failed_logins")
    private Integer numFailedLogins;

}
