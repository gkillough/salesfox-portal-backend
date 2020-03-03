package com.usepipeline.portal.database.authentication.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(schema = "portal", name = "organization_account_address_id")
public class OrganizationAccountAddressEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "organization_account_address_id")
    private Long organizationAccountAddressId;

    @PrimaryKeyJoinColumn
    @Column(name = "organization_account_id")
    private Long organizationAccountId;

    @Column(name = "street_number")
    private Integer streetNumber;

    @Column(name = "street_name")
    private String streetName;

    @Column(name = "apt_suite")
    private String aptSuite;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "zip_code")
    private String zipCode;

    @Column(name = "is_business")
    private Boolean isBusiness;

}
