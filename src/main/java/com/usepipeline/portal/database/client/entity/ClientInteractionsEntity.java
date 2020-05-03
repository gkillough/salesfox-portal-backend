package com.usepipeline.portal.database.client.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(schema = "portal", name = "client_interactions")
public class ClientInteractionsEntity {
    @Id
    @PrimaryKeyJoinColumn
    @Column(name = "client_id")
    private Long clientId;

    @Column(name = "contact_initiations")
    private Long contactInitiations;

    @Column(name = "engagements_generated")
    private Long engagementsGenerated;

}
