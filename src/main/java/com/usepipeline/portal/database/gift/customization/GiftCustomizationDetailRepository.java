package com.usepipeline.portal.database.gift.customization;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface GiftCustomizationDetailRepository extends JpaRepository<GiftCustomizationDetailEntity, UUID> {

}
