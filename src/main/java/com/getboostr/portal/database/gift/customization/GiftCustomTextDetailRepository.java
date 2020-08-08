package com.getboostr.portal.database.gift.customization;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface GiftCustomTextDetailRepository extends JpaRepository<GiftCustomTextDetailEntity, UUID> {
}
