package com.usepipeline.portal.database.organization.account.invite;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface OrganizationAccountInviteTokenRepository extends JpaRepository<OrganizationAccountInviteTokenEntity, OrganizationAccountInviteTokenPK> {
    List<OrganizationAccountInviteTokenEntity> findByEmail(String email);

    void deleteByEmail(String email);

}
