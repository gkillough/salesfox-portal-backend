package ai.salesfox.portal.rest.api.organization.profile;

import ai.salesfox.portal.common.FieldValidationUtils;
import ai.salesfox.portal.common.enumeration.AccessOperation;
import ai.salesfox.portal.common.model.PortalAddressModel;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.organization.OrganizationEntity;
import ai.salesfox.portal.database.organization.account.OrganizationAccountEntity;
import ai.salesfox.portal.database.organization.account.OrganizationAccountRepository;
import ai.salesfox.portal.database.organization.account.address.OrganizationAccountAddressEntity;
import ai.salesfox.portal.database.organization.account.address.OrganizationAccountAddressRepository;
import ai.salesfox.portal.database.organization.account.profile.OrganizationAccountProfileEntity;
import ai.salesfox.portal.database.organization.account.profile.OrganizationAccountProfileRepository;
import ai.salesfox.portal.rest.api.organization.common.OrganizationAccessService;
import ai.salesfox.portal.rest.api.organization.common.OrganizationValidationService;
import ai.salesfox.portal.rest.api.organization.profile.model.OrganizationAccountProfileModel;
import ai.salesfox.portal.rest.api.organization.profile.model.OrganizationAccountProfileUpdateModel;
import ai.salesfox.portal.rest.util.HttpSafeUserMembershipRetrievalService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
public class OrganizationProfileService {
    private final HttpSafeUserMembershipRetrievalService membershipRetrievalService;
    private final OrganizationAccessService organizationAccessService;
    private final OrganizationValidationService organizationValidationService;
    private final OrganizationAccountRepository organizationAccountRepository;
    private final OrganizationAccountProfileRepository organizationAccountProfileRepository;
    private final OrganizationAccountAddressRepository organizationAccountAddressRepository;

    @Autowired
    public OrganizationProfileService(HttpSafeUserMembershipRetrievalService membershipRetrievalService, OrganizationAccessService organizationAccessService, OrganizationValidationService organizationValidationService,
                                      OrganizationAccountRepository organizationAccountRepository, OrganizationAccountProfileRepository organizationAccountProfileRepository, OrganizationAccountAddressRepository organizationAccountAddressRepository) {
        this.membershipRetrievalService = membershipRetrievalService;
        this.organizationAccessService = organizationAccessService;
        this.organizationValidationService = organizationValidationService;
        this.organizationAccountRepository = organizationAccountRepository;
        this.organizationAccountProfileRepository = organizationAccountProfileRepository;
        this.organizationAccountAddressRepository = organizationAccountAddressRepository;
    }

    public OrganizationAccountProfileModel getProfile(UUID organizationAccountId) {
        OrganizationAccountEntity orgAccountEntity = organizationAccountRepository.findById(organizationAccountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        validatePermittedAccessLevel(orgAccountEntity, AccessOperation.READ);

        OrganizationEntity organizationEntity = membershipRetrievalService.getOrganizationEntity(orgAccountEntity);
        OrganizationAccountProfileEntity orgAcctProfileEntity = getOrganizationAccountProfileEntity(organizationAccountId);
        OrganizationAccountAddressEntity orgAccountAddressEntity = getOrgAccountAddressEntity(orgAcctProfileEntity);
        PortalAddressModel orgAcctAddress = PortalAddressModel.fromEntity(orgAccountAddressEntity);

        return new OrganizationAccountProfileModel(organizationEntity.getOrganizationName(), orgAccountEntity.getOrganizationAccountName(), orgAcctProfileEntity.getBusinessNumber(), orgAcctAddress);
    }

    @Transactional
    public void updateProfile(UUID organizationAccountId, OrganizationAccountProfileUpdateModel requestModel) {
        OrganizationAccountEntity orgAccountEntity = organizationAccountRepository.findById(organizationAccountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        validatePermittedAccessLevel(orgAccountEntity, AccessOperation.UPDATE);
        validateUpdateRequestModel(orgAccountEntity, requestModel);

        OrganizationAccountProfileEntity orgAcctProfileEntity = getOrganizationAccountProfileEntity(organizationAccountId);
        OrganizationAccountAddressEntity orgAccountAddressEntity = getOrgAccountAddressEntity(orgAcctProfileEntity);

        requestModel.getOrganizationAddress().copyFieldsToEntity(orgAccountAddressEntity);
        organizationAccountAddressRepository.save(orgAccountAddressEntity);

        orgAccountEntity.setOrganizationAccountName(requestModel.getOrganizationAccountName());
        organizationAccountRepository.save(orgAccountEntity);

        orgAcctProfileEntity.setBusinessNumber(requestModel.getBusinessPhoneNumber());
        organizationAccountProfileRepository.save(orgAcctProfileEntity);
    }

    private void validatePermittedAccessLevel(OrganizationAccountEntity orgAccountEntity, AccessOperation requestedAccessOperation) {
        UserEntity authenticatedUserEntity = membershipRetrievalService.getAuthenticatedUserEntity();
        boolean canAccess = organizationAccessService.canUserAccessOrganizationAccount(authenticatedUserEntity, orgAccountEntity, requestedAccessOperation);
        if (!canAccess) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    private OrganizationAccountProfileEntity getOrganizationAccountProfileEntity(UUID orgAccountId) {
        return organizationAccountProfileRepository.findFirstByOrganizationAccountId(orgAccountId)
                .orElseThrow(() -> {
                    log.error("Missing organization account profile for organizationAccountId: [{}]", orgAccountId);
                    return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }

    private OrganizationAccountAddressEntity getOrgAccountAddressEntity(OrganizationAccountProfileEntity profileEntity) {
        return organizationAccountAddressRepository.findById(profileEntity.getOrganizationAccountId())
                .orElseThrow(() -> {
                    log.error("Missing organization account profile for organizationProfileId: [{}]", profileEntity.getProfileId());
                    return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }

    private void validateUpdateRequestModel(OrganizationAccountEntity orgAccountEntity, OrganizationAccountProfileUpdateModel updateModel) {
        Set<String> errorFields = new LinkedHashSet<>();
        isBlankAddError(errorFields, "Organization Account Name", updateModel.getOrganizationAccountName());
        isBlankAddError(errorFields, "Business Phone Number", updateModel.getBusinessPhoneNumber());

        if (!orgAccountEntity.getOrganizationAccountName().equals(updateModel.getOrganizationAccountName())
                && organizationValidationService.isOrganizationAccountNameInUse(orgAccountEntity.getOrganizationId(), updateModel.getOrganizationAccountName())) {
            errorFields.add("That Organization Account Name is already in use");
        }

        if (!FieldValidationUtils.isValidUSPhoneNumber(updateModel.getBusinessPhoneNumber(), false)) {
            errorFields.add("That Organization Account Phone Number is in an invalid format");
        }

        if (updateModel.getOrganizationAddress() == null) {
            errorFields.add("Missing Organization Account Address details");
        } else if (!FieldValidationUtils.isValidUSAddress(updateModel.getOrganizationAddress(), true)) {
            errorFields.add("That Organization Account Address is invalid");
        }

        if (!errorFields.isEmpty()) {
            String errorFieldsString = String.join(", ", errorFields);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("There are errors with the fields: %s", errorFieldsString));
        }
    }

    private void isBlankAddError(Collection<String> errorFields, String fieldName, String fieldValue) {
        if (StringUtils.isBlank(fieldValue)) {
            errorFields.add(String.format("%s is blank", fieldName));
        }
    }

}
