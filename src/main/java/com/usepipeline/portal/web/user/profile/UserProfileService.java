package com.usepipeline.portal.web.user.profile;

import com.usepipeline.portal.common.exception.PortalRestException;
import com.usepipeline.portal.common.model.PortalAddressModel;
import com.usepipeline.portal.database.authentication.entity.ProfileEntity;
import com.usepipeline.portal.database.authentication.entity.UserAddressEntity;
import com.usepipeline.portal.database.authentication.entity.UserEntity;
import com.usepipeline.portal.database.authentication.repository.ProfileRepository;
import com.usepipeline.portal.database.authentication.repository.UserAddressRepository;
import com.usepipeline.portal.database.authentication.repository.UserRepository;
import com.usepipeline.portal.web.user.common.UserAccessService;
import com.usepipeline.portal.web.user.role.UserRoleModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

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

    public UserProfileModel getProfile(Long userId) throws PortalRestException {
        validateUserId(userId);

        // Any failed lookups after the above validation are fatal.
        Supplier<PortalRestException> exceptionSupplier = () -> new PortalRestException(HttpStatus.INTERNAL_SERVER_ERROR);

        ProfileEntity profileEntity = profileRepository.findByUserId(userId).orElse(null);
        if (profileEntity == null) {
            profileEntity = doInitializeProfile(userId)
                    .orElseThrow(exceptionSupplier);
        }

        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(exceptionSupplier);

        PortalAddressModel portalAddressModel = userAddressRepository.findById(profileEntity.getMailingAddressId())
                .map(PortalAddressModel::fromEntity)
                .orElseThrow(exceptionSupplier);

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

    public void updateProfile(Long userId, UserProfileModel updateModel) throws PortalRestException {
        validateUserId(userId);
        validateUpdateRequest(updateModel);


    }

    private Optional<ProfileEntity> doInitializeProfile(Long userId) {
        Optional<ProfileEntity> existingProfile = profileRepository.findByUserId(userId);
        if (existingProfile.isPresent()) {
            return Optional.empty();
        }

        UserAddressEntity newAddress = new UserAddressEntity(null);
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

    private void validateUserId(Long userId) throws PortalRestException {
        if (userId == null) {
            throw new PortalRestException(HttpStatus.BAD_REQUEST);
        }

        if (!userAccessService.canCurrentUserAccessDataForUser(userId)) {
            log.warn("There was an attempt to access the user with id [{}], from an unauthorized account", userId);
            throw new PortalRestException(HttpStatus.FORBIDDEN);
        }
    }

    private void validateUpdateRequest(UserProfileModel userProfileModel) throws PortalRestException {
        if (
                !isValidEmailAddress(userProfileModel.getEmail())
                        || !isValidNumber(userProfileModel.getMobileNumber())
                        || !isValidNumber(userProfileModel.getBusinessNumber())
                        || !isValidAddress(userProfileModel.getAddress())
        ) {
            throw new PortalRestException(HttpStatus.BAD_REQUEST);
        }
    }

    // TODO abstract these validators somewhere common:

    private boolean isValidEmailAddress(String emailAddress) {
        // Pattern from: https://howtodoinjava.com/regex/java-regex-validate-email-address/
        String validEmailPattern = "^[\\\\w!#$%&'*+/=?`{|}~^-]+(?:\\\\.[\\\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\\\.)+[a-zA-Z]{2,6}$";
        return StringUtils.isNotBlank(emailAddress) && emailAddress.matches(validEmailPattern);
    }

    private boolean isValidNumber(String phoneNumber) {
        return StringUtils.isBlank(phoneNumber) || NumberUtils.isDigits(phoneNumber);
    }

    private boolean isValidAddress(PortalAddressModel addressModel) {
        // TODO determine validation strategy
        return true;
    }

}
