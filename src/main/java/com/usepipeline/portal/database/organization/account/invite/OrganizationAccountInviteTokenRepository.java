package com.usepipeline.portal.database.organization.account.invite;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public interface OrganizationAccountInviteTokenRepository extends JpaRepository<OrganizationAccountInviteTokenEntity, OrganizationAccountInviteTokenPK> {
    void deleteByEmail(String email);

}
