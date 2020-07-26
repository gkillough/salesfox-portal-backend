package com.getboostr.portal.database.contact.interaction;

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
@IdClass(ContactInteractionPK.class)
@Table(schema = "portal", name = "contact_interactions")
public class ContactInteractionEntity implements Serializable, Contactable {
    @Id
    @PrimaryKeyJoinColumn
    @Column(name = "contact_id")
    private UUID contactId;

    @Id
    @PrimaryKeyJoinColumn
    @Column(name = "interaction_id")
    private UUID interactionId;

}
