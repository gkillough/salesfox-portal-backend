package com.usepipeline.portal.database.client.repository;

import com.usepipeline.portal.database.client.entity.ClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public interface ClientRepository extends JpaRepository<ClientEntity, Long> {

}
