package com.usepipeline.portal.database.authentication.repository;

import com.usepipeline.portal.database.authentication.entity.MembershipEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public interface MembershipRepository extends JpaRepository<MembershipEntity, Long> {
}
