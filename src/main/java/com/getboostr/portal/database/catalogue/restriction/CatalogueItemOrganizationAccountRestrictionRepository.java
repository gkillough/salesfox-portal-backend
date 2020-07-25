package com.getboostr.portal.database.catalogue.restriction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface CatalogueItemOrganizationAccountRestrictionRepository extends JpaRepository<CatalogueItemOrganizationAccountRestrictionEntity, UUID> {

}
