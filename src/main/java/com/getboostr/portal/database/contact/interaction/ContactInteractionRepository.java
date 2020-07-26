package com.getboostr.portal.database.contact.interaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public interface ContactInteractionRepository extends JpaRepository<ContactInteractionEntity, ContactInteractionPK> {

}
