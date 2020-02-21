package com.usepipeline.portal.database.authentication.repository;

import com.usepipeline.portal.database.authentication.entity.UserAddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public interface UserAddressRepository extends JpaRepository<UserAddressEntity, Long> {
}
