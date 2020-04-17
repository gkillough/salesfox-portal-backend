package com.usepipeline.portal.database.organization;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(schema = "portal", name = "organizations")
public class OrganizationEntity implements Serializable {
    @Id
    @SequenceGenerator(schema = "portal", name = "organizations_organization_id_seq_generator", sequenceName = "organizations_organization_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "organizations_organization_id_seq_generator")
    @Column(name = "organization_id")
    private Long organizationId;

    @Column(name = "organization_name")
    private String organizationName;

    @Column(name = "is_active")
    private Boolean isActive;

}
