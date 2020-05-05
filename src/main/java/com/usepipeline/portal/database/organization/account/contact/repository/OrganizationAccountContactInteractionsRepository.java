package com.usepipeline.portal.database.organization.account.contact.repository;

import com.usepipeline.portal.database.organization.account.contact.entity.OrganizationAccountContactInteractionsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public interface OrganizationAccountContactInteractionsRepository extends JpaRepository<OrganizationAccountContactInteractionsEntity, Long> {

}
