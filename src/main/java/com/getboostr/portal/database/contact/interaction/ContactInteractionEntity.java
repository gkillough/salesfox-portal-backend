package com.getboostr.portal.database.contact.interaction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(schema = "portal", name = "contact_interactions")
public class ContactInteractionEntity implements Serializable {
    @Id
    @PrimaryKeyJoinColumn
    @Column(name = "interaction_id")
    private UUID interactionId;

    @PrimaryKeyJoinColumn
    @Column(name = "contact_id")
    private UUID contactId;

    @PrimaryKeyJoinColumn
    @Column(name = "interacting_user_id")
    private UUID interactingUserId;

    @Column(name = "medium")
    private String medium;

    @Column(name = "classification")
    private String classification;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "note")
    private String note;

}
