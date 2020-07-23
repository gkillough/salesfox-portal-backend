package com.getboostr.portal.common.service.contact;

import com.getboostr.portal.common.enumeration.AccessOperation;
import com.getboostr.portal.common.service.auth.AbstractMembershipRetrievalService;
import com.getboostr.portal.database.account.entity.MembershipEntity;
import com.getboostr.portal.database.account.entity.UserEntity;
import com.getboostr.portal.database.contact.entity.ContactOrganizationAccountRestrictionEntity;
import com.getboostr.portal.database.contact.entity.ContactUserRestrictionEntity;
import com.getboostr.portal.database.contact.entity.OrganizationAccountContactEntity;
import com.getboostr.portal.database.contact.entity.OrganizationAccountContactProfileEntity;
import com.getboostr.portal.database.contact.repository.OrganizationAccountContactProfileRepository;
import com.getboostr.portal.rest.security.authorization.PortalAuthorityConstants;

import java.util.UUID;

public class ContactAccessOperationUtility<E extends Throwable> {
    private final AbstractMembershipRetrievalService<E> membershipRetrievalService;
    private final OrganizationAccountContactProfileRepository contactProfileRepository;

    public ContactAccessOperationUtility(AbstractMembershipRetrievalService<E> membershipRetrievalService, OrganizationAccountContactProfileRepository contactProfileRepository) {
        this.membershipRetrievalService = membershipRetrievalService;
        this.contactProfileRepository = contactProfileRepository;
    }

    public boolean canUserAccessContact(UserEntity userRequestingAccess, OrganizationAccountContactEntity contact, AccessOperation requestedAccessOperation) throws E {
        MembershipEntity userMembership = userRequestingAccess.getMembershipEntity();
        String userRoleLevel = userMembership.getRoleEntity().getRoleLevel();
        if (PortalAuthorityConstants.PORTAL_ADMIN.equals(userRoleLevel)) {
            return true;
        }

        ContactOrganizationAccountRestrictionEntity contactOrgAcctRestriction = contact.getContactOrganizationAccountRestrictionEntity();
        ContactUserRestrictionEntity contactUserRestriction = contact.getContactUserRestrictionEntity();
        if (contactOrgAcctRestriction != null && userMembership.getOrganizationAccountId().equals(contactOrgAcctRestriction.getOrganizationAccountId())) {
            return canOrganizationUserAccessContact(userRequestingAccess, userRoleLevel, contact, requestedAccessOperation);
        }
        return contactUserRestriction != null && userRequestingAccess.getUserId().equals(contactUserRestriction.getUserId());
    }

    private boolean canOrganizationUserAccessContact(UserEntity userRequestingAccess, String userRoleLevel, OrganizationAccountContactEntity contact, AccessOperation requestedAccessOperation) throws E {
        if (PortalAuthorityConstants.ORGANIZATION_ACCOUNT_OWNER.equals(userRoleLevel) || PortalAuthorityConstants.ORGANIZATION_ACCOUNT_MANAGER.equals(userRoleLevel)) {
            return true;
        }

        switch (requestedAccessOperation) {
            case READ:
                return true;
            case UPDATE:
            case INTERACT:
                OrganizationAccountContactProfileEntity contactProfile = getPointOfContactProfile(contact);
                UUID pointOfContactUserId = contactProfile.getOrganizationPointOfContactUserId();
                return pointOfContactUserId == null || userRequestingAccess.getUserId().equals(pointOfContactUserId);
            default:
                return false;
        }
    }

    private OrganizationAccountContactProfileEntity getPointOfContactProfile(OrganizationAccountContactEntity contact) throws E {
        return contactProfileRepository.findByContactId(contact.getContactId())
                .orElseThrow(membershipRetrievalService::unexpectedErrorDuringRetrieval);
    }

}
