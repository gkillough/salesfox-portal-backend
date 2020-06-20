package com.usepipeline.portal.database.catalogue.restriction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public interface CatalogueItemRestrictionRepository extends JpaRepository<CatalogueItemRestrictionEntity, UUID> {
    Optional<CatalogueItemRestrictionEntity> findByItemId(UUID itemId);

}