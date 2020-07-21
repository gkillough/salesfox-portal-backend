package com.getboostr.portal.common.service.contact;

import com.getboostr.portal.common.enumeration.AccessOperation;
import com.getboostr.portal.database.account.entity.MembershipEntity;
import com.getboostr.portal.database.account.entity.UserEntity;
import com.getboostr.portal.common.service.auth.AbstractMembershipRetrievalService;
import com.getboostr.portal.database.organization.account.contact.entity.OrganizationAccountContactEntity;
import com.getboostr.portal.database.organization.account.contact.entity.OrganizationAccountContactProfileEntity;
import com.getboostr.portal.database.organization.account.contact.repository.OrganizationAccountContactProfileRepository;
import com.getboostr.portal.rest.security.authorization.PortalAuthorityConstants;

import java.util.UUID;

public class ContactAccessOperationUtility<E extends Throwable> {
    private AbstractMembershipRetrievalService<E> membershipRetrievalService;
    private OrganizationAccountContactProfileRepository contactProfileRepository;

    public ContactAccessOperationUtility(AbstractMembershipRetrievalService<E> membershipRetrievalService, OrganizationAccountContactProfileRepository contactProfileRepository) {
        this.membershipRetrievalService = membershipRetrievalService;
        this.contactProfileRepository = contactProfileRepository;
    }

    public boolean canUserAccessContact(UserEntity userRequestingAccess, OrganizationAccountContactEntity contact, AccessOperation requestedAccessOperation) throws E {
        MembershipEntity userMembership = membershipRetrievalService.getMembershipEntity(userRequestingAccess);

        String userRoleLevel = membershipRetrievalService.getRoleEntity(userMembership).getRoleLevel();
        if (PortalAuthorityConstants.PORTAL_ADMIN.equals(userRoleLevel)) {
            return true;
        } else if (userMembership.getOrganizationAccountId().equals(contact.getOrganizationAccountId())) {
            if (userRoleLevel.startsWith(PortalAuthorityConstants.ORGANIZATION_ROLE_PREFIX)) {
                return canOrganizationUserAccessContact(userRequestingAccess, userRoleLevel, contact, requestedAccessOperation);
            } else {
                return canNonOrganizationUserAccessContact(userRequestingAccess, userRoleLevel, contact);
            }
        }
        return false;
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

    private boolean canNonOrganizationUserAccessContact(UserEntity userRequestingAccess, String userRoleLevel, OrganizationAccountContactEntity contact) throws E {
        if (PortalAuthorityConstants.PORTAL_PREMIUM_USER.equals(userRoleLevel) || PortalAuthorityConstants.PORTAL_BASIC_USER.equals(userRoleLevel)) {
            OrganizationAccountContactProfileEntity contactProfile = getPointOfContactProfile(contact);
            return userRequestingAccess.getUserId().equals(contactProfile.getOrganizationPointOfContactUserId());
        }
        // In the future, there may be other roles that require granular access control (e.g. support or beta testers).
        return false;
    }

    private OrganizationAccountContactProfileEntity getPointOfContactProfile(OrganizationAccountContactEntity contact) throws E {
        return contactProfileRepository.findByContactId(contact.getContactId())
                .orElseThrow(membershipRetrievalService::unexpectedErrorDuringRetrieval);
    }

}
