package ai.salesfox.portal.common.service.contact;

import ai.salesfox.portal.common.enumeration.AccessOperation;
import ai.salesfox.portal.database.account.entity.MembershipEntity;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.contact.OrganizationAccountContactEntity;
import ai.salesfox.portal.database.contact.OrganizationAccountContactRepository;
import ai.salesfox.portal.rest.security.authorization.PortalAuthorityConstants;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

// TODO make this a service
public class ContactAccessOperationUtility {
    private final OrganizationAccountContactRepository contactRepository;

    public ContactAccessOperationUtility(OrganizationAccountContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    public boolean canUserAccessContact(UserEntity userRequestingAccess, OrganizationAccountContactEntity contact, AccessOperation requestedAccessOperation) {
        return canUserAccessContacts(userRequestingAccess, List.of(contact.getContactId()), requestedAccessOperation);
    }

    public boolean canUserAccessContacts(UserEntity userRequestingAccess, Collection<UUID> contactIds, AccessOperation requestedAccessOperation) {
        MembershipEntity userMembership = userRequestingAccess.getMembershipEntity();
        String userRoleLevel = userMembership.getRoleEntity().getRoleLevel();
        if (PortalAuthorityConstants.PORTAL_ADMIN.equals(userRoleLevel)) {
            return true;
        }

        int accessibleContactCount;
        if (!isOrgAcctOwnerOrManager(userRoleLevel)) {
            if (AccessOperation.UPDATE.equals(requestedAccessOperation) || AccessOperation.INTERACT.equals(requestedAccessOperation)) {
                accessibleContactCount = contactRepository.countInteractableContactsInContactIdCollection(userRequestingAccess.getUserId(), userMembership.getOrganizationAccountId(), contactIds);
            } else {
                return false;
            }
        } else {
            accessibleContactCount = contactRepository.countVisibleContactsInContactIdCollection(userRequestingAccess.getUserId(), userMembership.getOrganizationAccountId(), contactIds);
        }
        return accessibleContactCount == contactIds.size();
    }

    private boolean isOrgAcctOwnerOrManager(String userRoleLevel) {
        return PortalAuthorityConstants.ORGANIZATION_ACCOUNT_OWNER.equals(userRoleLevel) || PortalAuthorityConstants.ORGANIZATION_ACCOUNT_MANAGER.equals(userRoleLevel);
    }

}
