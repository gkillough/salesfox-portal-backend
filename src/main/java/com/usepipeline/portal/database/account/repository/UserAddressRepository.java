package com.usepipeline.portal.database.account.repository;

import com.usepipeline.portal.database.account.entity.UserAddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface UserAddressRepository extends JpaRepository<UserAddressEntity, UUID> {
}
