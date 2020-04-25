package com.usepipeline.portal.web.organization.profile;

import com.usepipeline.portal.common.FieldValidationUtils;
import com.usepipeline.portal.common.enumeration.AccessLevel;
import com.usepipeline.portal.common.model.PortalAddressModel;
import com.usepipeline.portal.database.account.entity.UserEntity;
import com.usepipeline.portal.database.organization.OrganizationEntity;
import com.usepipeline.portal.database.organization.account.OrganizationAccountEntity;
import com.usepipeline.portal.database.organization.account.OrganizationAccountRepository;
import com.usepipeline.portal.database.organization.account.address.OrganizationAccountAddressEntity;
import com.usepipeline.portal.database.organization.account.address.OrganizationAccountAddressRepository;
import com.usepipeline.portal.database.organization.account.profile.OrganizationAccountProfileEntity;
import com.usepipeline.portal.database.organization.account.profile.OrganizationAccountProfileRepository;
import com.usepipeline.portal.web.organization.common.OrganizationAccessService;
import com.usepipeline.portal.web.organization.common.OrganizationValidationService;
import com.usepipeline.portal.web.organization.profile.model.OrganizationAccountProfileModel;
import com.usepipeline.portal.web.organization.profile.model.OrganizationAccountProfileUpdateModel;
import com.usepipeline.portal.web.util.HttpSafeUserMembershipRetrievalService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

@Slf4j
@Component
public class OrganizationProfileService {
    private HttpSafeUserMembershipRetrievalService membershipRetrievalService;
    private OrganizationAccessService organizationAccessService;
    private OrganizationValidationService organizationValidationService;
    private OrganizationAccountRepository organizationAccountRepository;
    private OrganizationAccountProfileRepository organizationAccountProfileRepository;
    private OrganizationAccountAddressRepository organizationAccountAddressRepository;

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

    public OrganizationAccountProfileModel getProfile(Long organizationAccountId) {
        OrganizationAccountEntity orgAccountEntity = organizationAccountRepository.findById(organizationAccountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        AccessLevel accessLevel = getPermittedAccessLevel(orgAccountEntity);
        if (AccessLevel.NONE.equals(accessLevel)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        OrganizationEntity organizationEntity = membershipRetrievalService.getOrganizationEntity(orgAccountEntity);
        OrganizationAccountProfileEntity orgAcctProfileEntity = getOrganizationAccountProfileEntity(organizationAccountId);
        OrganizationAccountAddressEntity orgAccountAddressEntity = getOrgAccountAddressEntity(orgAcctProfileEntity);
        PortalAddressModel orgAcctAddress = PortalAddressModel.fromEntity(orgAccountAddressEntity);

        return new OrganizationAccountProfileModel(organizationEntity.getOrganizationName(), orgAccountEntity.getOrganizationAccountName(), orgAcctProfileEntity.getBusinessNumber(), orgAcctAddress);
    }

    @Transactional
    public void updateProfile(Long organizationAccountId, OrganizationAccountProfileUpdateModel requestModel) {
        OrganizationAccountEntity orgAccountEntity = organizationAccountRepository.findById(organizationAccountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        AccessLevel accessLevel = getPermittedAccessLevel(orgAccountEntity);
        boolean hasSufficientWriteAccess = AccessLevel.FULL.equals(accessLevel) || AccessLevel.READ_WRITE_INSENSITIVE.equals(accessLevel);
        if (!hasSufficientWriteAccess) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

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

    private AccessLevel getPermittedAccessLevel(OrganizationAccountEntity orgAccountEntity) {
        UserEntity authenticatedUserEntity = membershipRetrievalService.getAuthenticatedUserEntity();
        return organizationAccessService.getAccessLevelForUserRequestingAccount(authenticatedUserEntity, orgAccountEntity);
    }

    private OrganizationAccountProfileEntity getOrganizationAccountProfileEntity(Long orgAccountId) {
        return organizationAccountProfileRepository.findFirstByOrganizationAccountId(orgAccountId)
                .orElseThrow(() -> {
                    log.error("Missing organization account profile for organizationAccountId: [{}]", orgAccountId);
                    return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }

    private OrganizationAccountAddressEntity getOrgAccountAddressEntity(OrganizationAccountProfileEntity profileEntity) {
        return organizationAccountAddressRepository.findById(profileEntity.getMailingAddressId())
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
