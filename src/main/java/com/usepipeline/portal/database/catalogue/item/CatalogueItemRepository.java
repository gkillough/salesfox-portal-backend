package com.usepipeline.portal.database.catalogue.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface CatalogueItemRepository extends JpaRepository<CatalogueItemEntity, UUID> {

}
