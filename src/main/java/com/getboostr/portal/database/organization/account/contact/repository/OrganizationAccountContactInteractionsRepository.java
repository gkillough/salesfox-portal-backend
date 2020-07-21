package com.getboostr.portal.database.organization.account.contact.repository;

import com.getboostr.portal.database.organization.account.contact.entity.OrganizationAccountContactInteractionsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface OrganizationAccountContactInteractionsRepository extends JpaRepository<OrganizationAccountContactInteractionsEntity, UUID> {

}
