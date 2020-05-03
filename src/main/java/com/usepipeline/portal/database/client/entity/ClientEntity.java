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
    // FIXME for bulk upload, allocationSize=1 will likely not be sufficient
    //  consider a different generation strategy
    @SequenceGenerator(schema = "portal", name = "clients_client_id_seq_generator", sequenceName = "clients_client_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "clients_client_id_seq_generator")
    @Column(name = "client_id")
    private Long clientId;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "organization_name")
    private String organizationName;

    @Column(name = "title")
    private String title;

    @Column(name = "email")
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "organization_point_of_contact")
    private String organizationPointOfContact;

    @Column(name = "is_active")
    private Boolean isActive;

}
