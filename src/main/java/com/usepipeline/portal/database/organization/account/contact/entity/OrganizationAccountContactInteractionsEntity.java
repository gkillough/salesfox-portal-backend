package com.usepipeline.portal.database.organization.account.contact.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(schema = "portal", name = "organization_account_contact_interactions")
public class OrganizationAccountContactInteractionsEntity {
    @Id
    @PrimaryKeyJoinColumn
    @Column(name = "contact_id")
    private Long contactId;

    @Column(name = "contact_initiations")
    private Long contactInitiations;

    @Column(name = "engagements_generated")
    private Long engagementsGenerated;

}
