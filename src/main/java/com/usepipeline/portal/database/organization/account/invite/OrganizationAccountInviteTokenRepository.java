package com.usepipeline.portal.database.organization.account.invite;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationAccountInviteTokenRepository extends JpaRepository<OrganizationAccountInviteTokenEntity, OrganizationAccountInviteTokenPK> {
    void deleteByEmail(String email);

}
