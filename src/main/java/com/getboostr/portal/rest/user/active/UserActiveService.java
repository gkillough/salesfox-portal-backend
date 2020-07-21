package com.getboostr.portal.rest.user.active;

import com.getboostr.portal.common.exception.PortalDatabaseIntegrityViolationException;
import com.getboostr.portal.common.service.license.LicenseSeatManager;
import com.getboostr.portal.common.service.license.PortalLicenseSeatException;
import com.getboostr.portal.database.account.entity.LicenseEntity;
import com.getboostr.portal.database.account.entity.MembershipEntity;
import com.getboostr.portal.database.account.entity.UserEntity;
import com.getboostr.portal.database.account.repository.UserRepository;
import com.getboostr.portal.database.organization.account.OrganizationAccountEntity;
import com.getboostr.portal.rest.common.model.request.ActiveStatusPatchModel;
import com.getboostr.portal.rest.user.common.UserAccessService;
import com.getboostr.portal.rest.util.HttpSafeUserMembershipRetrievalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.util.UUID;

@Component
public class UserActiveService {
    private UserAccessService userAccessService;
    private UserRepository userRepository;
    private HttpSafeUserMembershipRetrievalService membershipRetrievalService;
    private LicenseSeatManager licenseSeatManager;

    @Autowired
    public UserActiveService(UserAccessService userAccessService, UserRepository userRepository, HttpSafeUserMembershipRetrievalService membershipRetrievalService, LicenseSeatManager licenseSeatManager) {
        this.userAccessService = userAccessService;
        this.userRepository = userRepository;
        this.membershipRetrievalService = membershipRetrievalService;
        this.licenseSeatManager = licenseSeatManager;
    }

    @Transactional
    public void updateUserActiveStatus(UUID userId, ActiveStatusPatchModel updateModel) {
        if (updateModel.getActiveStatus() == null) {
            throw createBadRequest("The field 'activeStatus' cannot be null");
        }

        if (!userAccessService.canCurrentUserAccessDataForUser(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        updateUserActiveStatusWithoutPermissionCheck(userId, updateModel.getActiveStatus());
    }

    @Transactional
    public void updateUserActiveStatusWithoutPermissionCheck(UUID userId, boolean activeStatus) {
        if (userId == null) {
            throw createBadRequest("The field 'userId' cannot be null");
        }

        UserEntity requestedUser = userRepository.findById(userId)
                .orElseThrow(() -> createBadRequest(String.format("A user with the id ['%s'] does not exist", userId)));

        MembershipEntity requestedUserMembership = membershipRetrievalService.getMembershipEntity(requestedUser);
        OrganizationAccountEntity organizationAccount = membershipRetrievalService.getOrganizationAccountEntity(requestedUserMembership);
        toggleLicenseSeatForUser(organizationAccount, activeStatus);

        requestedUser.setIsActive(activeStatus);
        userRepository.save(requestedUser);
    }

    private ResponseStatusException createBadRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private void toggleLicenseSeatForUser(OrganizationAccountEntity orgAccount, boolean isActive) {
        try {
            LicenseEntity orgLicense = licenseSeatManager.getLicenseForOrganizationAccount(orgAccount);
            if (isActive) {
                licenseSeatManager.fillSeat(orgLicense);
            } else {
                licenseSeatManager.vacateSeat(orgLicense);
            }
        } catch (PortalDatabaseIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (PortalLicenseSeatException e) {
            throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED, e.getMessage());
        }
    }

}
