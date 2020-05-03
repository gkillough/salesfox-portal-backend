package com.usepipeline.portal.database.client.repository;

import com.usepipeline.portal.database.client.entity.ClientProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public interface ClientProfileRepository extends JpaRepository<ClientProfileEntity, Long> {

}
