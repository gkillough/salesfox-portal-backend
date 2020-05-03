package com.usepipeline.portal.database.client.repository;

import com.usepipeline.portal.database.client.entity.ClientAddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public interface ClientAddressRepository extends JpaRepository<ClientAddressEntity, Long> {

}
