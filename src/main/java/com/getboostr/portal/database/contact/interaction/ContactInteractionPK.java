package com.getboostr.portal.database.contact.interaction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactInteractionPK implements Serializable {
    private UUID contactId;
    private UUID interactionId;

}
