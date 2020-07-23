package com.getboostr.portal.database.contact.entity;

import com.getboostr.portal.database.contact.Contactable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(schema = "portal", name = "organization_account_contact_interactions")
public class OrganizationAccountContactInteractionsEntity implements Serializable, Contactable {
    @Id
    @PrimaryKeyJoinColumn
    @Column(name = "contact_id")
    private UUID contactId;

    @Column(name = "contact_initiations")
    private Long contactInitiations;

    @Column(name = "engagements_generated")
    private Long engagementsGenerated;

}
