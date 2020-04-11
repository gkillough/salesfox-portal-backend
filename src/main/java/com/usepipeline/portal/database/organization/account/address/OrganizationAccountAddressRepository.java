package com.usepipeline.portal.database.organization.account.address;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public interface OrganizationAccountAddressRepository extends JpaRepository<OrganizationAccountAddressEntity, Long> {
}
