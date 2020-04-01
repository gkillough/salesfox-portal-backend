package com.usepipeline.portal.web.user.profile;

import com.usepipeline.portal.common.FieldValidationUtils;
import com.usepipeline.portal.common.model.PortalAddressModel;
import com.usepipeline.portal.database.account.entity.ProfileEntity;
import com.usepipeline.portal.database.account.entity.UserAddressEntity;
import com.usepipeline.portal.database.account.entity.UserEntity;
import com.usepipeline.portal.database.account.repository.ProfileRepository;
import com.usepipeline.portal.database.account.repository.UserAddressRepository;
import com.usepipeline.portal.database.account.repository.UserRepository;
import com.usepipeline.portal.web.user.common.UserAccessService;
import com.usepipeline.portal.web.user.profile.model.UserProfileModel;
import com.usepipeline.portal.web.user.profile.model.UserProfileUpdateModel;
import com.usepipeline.portal.web.user.role.model.UserRoleModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.util.Optional;
import java.util.function.Supplier;

@Slf4j
@Component
public class UserProfileService {
    private ProfileRepository profileRepository;
    private UserAddressRepository userAddressRepository;
    private UserRepository userRepository;
    private UserAccessService userAccessService;

    @Autowired
    public UserProfileService(ProfileRepository profileRepository, UserAddressRepository userAddressRepository, UserRepository userRepository, UserAccessService userAccessService) {
        this.profileRepository = profileRepository;
        this.userAddressRepository = userAddressRepository;
        this.userRepository = userRepository;
        this.userAccessService = userAccessService;
    }

    /**
     * @return an optional profile id if a profile was created
     */
    @Transactional
    public Optional<Long> initializeProfile(Long userId) {
        if (userId == null) {
            return Optional.empty();
        }
        return doInitializeProfile(userId)
                .map(ProfileEntity::getProfileId);
    }

    public UserProfileModel getProfile(Long userId) {
        validateUserId(userId);

        // Any failed lookups after the above validation are fatal.
        Supplier<ResponseStatusException> internalServerError = () -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);

        ProfileEntity profileEntity = profileRepository.findByUserId(userId).orElse(null);
        if (profileEntity == null) {
            profileEntity = doInitializeProfile(userId)
                    .orElseThrow(internalServerError);
        }

        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(internalServerError);

        PortalAddressModel portalAddressModel = userAddressRepository.findById(profileEntity.getMailingAddressId())
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
    public void updateProfile(Long userId, UserProfileUpdateModel updateModel) {
        validateUserId(userId);
        validateUpdateRequest(updateModel);

        // Any failed lookups after the above validation are fatal.
        Supplier<ResponseStatusException> internalServerError = () -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);

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

        ProfileEntity oldProfile = profileRepository.findByUserId(userId)
                .orElseThrow(internalServerError);

        ProfileEntity profileEntityToSave = new ProfileEntity(
                oldProfile.getProfileId(),
                oldUser.getUserId(),
                updateModel.getMobileNumber(),
                updateModel.getBusinessNumber(),
                oldProfile.getMailingAddressId()
        );
        profileRepository.save(profileEntityToSave);

        PortalAddressModel addressModel = updateModel.getAddress();
        UserAddressEntity userAddressToSave = new UserAddressEntity(oldProfile.getMailingAddressId(), oldUser.getUserId());
        addressModel.copyFieldsToEntity(userAddressToSave);
        userAddressRepository.save(userAddressToSave);
    }

    public boolean isEmailAlreadyInUse(String emailAddress) {
        return userRepository.findFirstByEmail(emailAddress).isPresent();
    }

    private Optional<ProfileEntity> doInitializeProfile(Long userId) {
        Optional<ProfileEntity> existingProfile = profileRepository.findByUserId(userId);
        if (existingProfile.isPresent()) {
            return Optional.empty();
        }

        UserAddressEntity newAddress = new UserAddressEntity(null, userId);
        newAddress.setStreetNumber(0);
        newAddress.setStreetName("");
        newAddress.setAptSuite("");
        newAddress.setCity("");
        newAddress.setState("");
        newAddress.setZipCode("");
        newAddress.setIsBusiness(false);
        UserAddressEntity savedAddress = userAddressRepository.save(newAddress);

        ProfileEntity newProfile = new ProfileEntity(null, userId, "", "", savedAddress.getUserAddressId());
        ProfileEntity savedProfile = profileRepository.save(newProfile);
        return Optional.of(savedProfile);
    }

    private void validateUserId(Long userId) {
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
        } else if (!FieldValidationUtils.isValidUSAddress(updateModel.getAddress(), true)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid address");
        }
    }

}
