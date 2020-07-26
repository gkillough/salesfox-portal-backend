package com.getboostr.portal.database.interactions;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface InteractionsRepository extends JpaRepository<InteractionEntity, UUID> {
}
