package ai.salesfox.portal.rest.api.user.profile;

import ai.salesfox.portal.common.FieldValidationUtils;
import ai.salesfox.portal.common.model.PortalAddressModel;
import ai.salesfox.portal.database.account.entity.ProfileEntity;
import ai.salesfox.portal.database.account.entity.UserAddressEntity;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.account.repository.ProfileRepository;
import ai.salesfox.portal.database.account.repository.UserAddressRepository;
import ai.salesfox.portal.database.account.repository.UserRepository;
import ai.salesfox.portal.rest.api.user.common.UserAccessService;
import ai.salesfox.portal.rest.api.user.profile.model.UserProfileModel;
import ai.salesfox.portal.rest.api.user.profile.model.UserProfileUpdateModel;
import ai.salesfox.portal.rest.api.user.role.model.UserRoleModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

@Slf4j
@Component
public class UserProfileService {
    private final ProfileRepository profileRepository;
    private final UserAddressRepository userAddressRepository;
    private final UserRepository userRepository;
    private final UserAccessService userAccessService;

    @Autowired
    public UserProfileService(ProfileRepository profileRepository, UserAddressRepository userAddressRepository, UserRepository userRepository, UserAccessService userAccessService) {
        this.profileRepository = profileRepository;
        this.userAddressRepository = userAddressRepository;
        this.userRepository = userRepository;
        this.userAccessService = userAccessService;
    }

    @Transactional
    public void initializeProfile(UUID userId) {
        if (userId != null) {
            getOrInitializeProfile(userId);
        }
    }

    public UserProfileModel retrieveProfile(UUID userId) {
        validateUserId(userId);
        return retrieveProfileWithoutPermissionCheck(userId);
    }

    /**
     * For use when permission to perform this action has already been validated, but that
     * validation is not reflected in the HTTP Session (e.g. during Organization Account Owner retrieval).
     */
    public UserProfileModel retrieveProfileWithoutPermissionCheck(UUID userId) {
        // Any failed lookups after the above validation are fatal.
        Supplier<ResponseStatusException> internalServerError = () -> {
            log.error("Missing database entity when attempting to retrieve a user profile");
            return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        };

        ProfileEntity profileEntity = getOrInitializeProfile(userId);

        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(internalServerError);

        PortalAddressModel portalAddressModel = userAddressRepository.findById(userEntity.getUserId())
                .map(PortalAddressModel::fromEntity)
                .orElseThrow(internalServerError);

        UserRoleModel role = userAccessService.findRoleByUserId(userId);

        return new UserProfileModel(
                userEntity.getFirstName(),
                userEntity.getLastName(),
                userEntity.getEmail(),
                portalAddressModel,
                profileEntity.getMobileNumber(),
                profileEntity.getBusinessNumber(),
                userEntity.getIsActive(),
                role
        );
    }

    @Transactional
    public void updateProfile(UUID userId, UserProfileUpdateModel updateModel) {
        validateUserId(userId);
        updateProfileWithoutPermissionsCheck(userId, updateModel);
    }

    /**
     * For use when permission to perform this action has already been validated, but that
     * validation is not reflected in the HTTP Session (e.g. during Organization Account creation).
     */
    @Transactional
    public void updateProfileWithoutPermissionsCheck(@NotNull UUID userId, UserProfileUpdateModel updateModel) {
        validateUpdateRequest(updateModel);

        // Any failed lookups after the above validation are fatal.
        Supplier<ResponseStatusException> internalServerError = () -> {
            log.error("User id not present in the database: [{}]", userId);
            return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        };

        UserEntity oldUser = userRepository.findById(userId)
                .orElseThrow(internalServerError);
        if (!oldUser.getEmail().equals(updateModel.getEmail())) {
            if (isEmailAlreadyInUse(updateModel.getEmail())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This email address is already in use");
            }
            // TODO consider sending a confirmation link
        }

        UserEntity userToSave = new UserEntity(oldUser.getUserId(), updateModel.getEmail(), updateModel.getFirstName(), updateModel.getLastName(), oldUser.getIsActive());
        userRepository.save(userToSave);

        ProfileEntity profileEntityToSave = new ProfileEntity(
                oldUser.getUserId(),
                updateModel.getMobileNumber(),
                updateModel.getBusinessNumber()
        );
        profileRepository.save(profileEntityToSave);

        PortalAddressModel addressModel = updateModel.getAddress();
        UserAddressEntity userAddressToSave = new UserAddressEntity(oldUser.getUserId());
        addressModel.copyFieldsToEntity(userAddressToSave);
        userAddressRepository.save(userAddressToSave);
    }

    public boolean isEmailAlreadyInUse(String emailAddress) {
        return userRepository.findFirstByEmail(emailAddress).isPresent();
    }

    private ProfileEntity getOrInitializeProfile(UUID userId) {
        Optional<ProfileEntity> existingProfile = profileRepository.findById(userId);
        if (existingProfile.isPresent()) {
            return existingProfile.get();
        }

        UserAddressEntity newAddress = new UserAddressEntity(userId);
        newAddress.setAddressLine1("");
        newAddress.setAddressLine2("");
        newAddress.setCity("");
        newAddress.setState("");
        newAddress.setZipCode("");
        newAddress.setIsBusiness(false);
        userAddressRepository.save(newAddress);

        ProfileEntity newProfile = new ProfileEntity(userId, "", "");
        return profileRepository.save(newProfile);
    }

    private void validateUserId(UUID userId) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        if (!userAccessService.canCurrentUserAccessDataForUser(userId)) {
            log.warn("There was an attempt to access the user with id [{}], from an unauthorized account", userId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    private void validateUpdateRequest(UserProfileUpdateModel updateModel) {
        if (!FieldValidationUtils.isValidEmailAddress(updateModel.getEmail(), false)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid email");
        } else if (!FieldValidationUtils.isValidUSPhoneNumber(updateModel.getMobileNumber(), true)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid mobile number");
        } else if (!FieldValidationUtils.isValidUSPhoneNumber(updateModel.getBusinessNumber(), true)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid business number");
        } else if (!FieldValidationUtils.isValidUSAddress(updateModel.getAddress(), false)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid address");
        }
    }

}
