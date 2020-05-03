package com.usepipeline.portal.database.client.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(schema = "portal", name = "clients")
public class ClientEntity implements Serializable {
    @Id
    @SequenceGenerator(schema = "portal", name = "clients_client_id_seq_generator", sequenceName = "clients_client_id_seq", allocationSize = 100, initialValue = 100)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "clients_client_id_seq_generator")
    @Column(name = "client_id")
    private Long clientId;

    @PrimaryKeyJoinColumn
    @Column(name = "organization_account_id")
    private Long organizationAccountId;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "email")
    private String email;

    @Column(name = "is_active")
    private Boolean isActive;

}
