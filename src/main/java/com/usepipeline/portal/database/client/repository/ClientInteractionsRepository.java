package com.usepipeline.portal.database.client.repository;

import com.usepipeline.portal.database.client.entity.ClientInteractionsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public interface ClientInteractionsRepository extends JpaRepository<ClientInteractionsEntity, Long> {

}
