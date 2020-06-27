package com.usepipeline.portal.database.gift;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface GiftRepository extends JpaRepository<GiftEntity, UUID> {

}
