package ai.salesfox.portal.rest.api.organization.activation;

import ai.salesfox.portal.database.account.entity.MembershipEntity;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.account.repository.MembershipRepository;
import ai.salesfox.portal.database.account.repository.UserRepository;
import ai.salesfox.portal.database.common.PageUtils;
import ai.salesfox.portal.database.organization.account.OrganizationAccountEntity;
import ai.salesfox.portal.database.organization.account.OrganizationAccountRepository;
import ai.salesfox.portal.rest.api.common.model.request.ActiveStatusPatchModel;
import ai.salesfox.portal.rest.api.license.LicenseService;
import ai.salesfox.portal.rest.api.user.active.UserActiveService;
import ai.salesfox.portal.rest.util.HttpSafeUserMembershipRetrievalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class OrganizationActivationService {
    private HttpSafeUserMembershipRetrievalService membershipRetrievalService;
    private MembershipRepository membershipRepository;
    private UserRepository userRepository;
    private OrganizationAccountRepository organizationAccountRepository;
    private LicenseService licenseService;
    private UserActiveService userActiveService;

    @Autowired
    public OrganizationActivationService(HttpSafeUserMembershipRetrievalService membershipRetrievalService, MembershipRepository membershipRepository, UserRepository userRepository,
                                         OrganizationAccountRepository organizationAccountRepository, LicenseService licenseService, UserActiveService userActiveService) {
        this.membershipRetrievalService = membershipRetrievalService;
        this.membershipRepository = membershipRepository;
        this.userRepository = userRepository;
        this.organizationAccountRepository = organizationAccountRepository;
        this.licenseService = licenseService;
        this.userActiveService = userActiveService;
    }

    @Transactional
    public void updateOrganizationAccountActiveStatus(UUID organizationAccountId, ActiveStatusPatchModel activeStatusModel) {
        validateUpdatePermission(organizationAccountId);
        if (activeStatusModel.getActiveStatus() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The field 'activeStatus' cannot be null");
        }

        OrganizationAccountEntity orgAccountEntity = organizationAccountRepository.findById(organizationAccountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        licenseService.setActiveStatus(orgAccountEntity.getLicenseId(), activeStatusModel);

        // TODO consider making the rest of this method asynchronous and returning
        setActiveStatusForOrgUsers(organizationAccountId, activeStatusModel.getActiveStatus());

        orgAccountEntity.setIsActive(activeStatusModel.getActiveStatus());
        organizationAccountRepository.save(orgAccountEntity);
    }

    private void validateUpdatePermission(UUID organizationAccountId) {
        if (!membershipRetrievalService.isAuthenticatedUserPortalAdmin()) {
            UserEntity authenticatedUserEntity = membershipRetrievalService.getAuthenticatedUserEntity();
            MembershipEntity membershipEntity = membershipRetrievalService.getMembershipEntity(authenticatedUserEntity);
            if (membershipEntity.getOrganizationAccountId() != organizationAccountId) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
        }
    }

    private void setActiveStatusForOrgUsers(UUID orgAccountId, boolean activeStatus) {
        Function<PageRequest, Page<MembershipEntity>> requestPageOfMemberships = pageRequest -> membershipRepository.findByOrganizationAccountId(orgAccountId, pageRequest);
        List<UUID> orgMemberUserIds = PageUtils.retrieveAll(requestPageOfMemberships)
                .stream()
                .map(MembershipEntity::getUserId)
                .collect(Collectors.toList());

        List<UserEntity> users = userRepository.findAllById(orgMemberUserIds);
        for (UserEntity user : users) {
            // TODO figure out how to avoid reactivating "permanently" deactivated users
            userActiveService.updateUserActiveStatusWithoutPermissionCheck(user.getUserId(), activeStatus);
        }
    }

}
