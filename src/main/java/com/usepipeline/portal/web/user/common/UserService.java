package com.usepipeline.portal.web.user.common;

import com.usepipeline.portal.common.exception.PortalRestException;
import com.usepipeline.portal.database.authentication.entity.UserEntity;
import com.usepipeline.portal.database.authentication.repository.UserRepository;
import com.usepipeline.portal.web.security.authentication.SecurityContextUtils;
import com.usepipeline.portal.web.user.common.model.CurrentUserModel;
import com.usepipeline.portal.web.user.common.model.UserAccountModel;
import com.usepipeline.portal.web.user.role.UserRoleModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class UserService {
    private UserRepository userRepository;
    private UserAccessService userAccessService;

    @Autowired
    public UserService(UserRepository userRepository, UserAccessService userAccessService) {
        this.userRepository = userRepository;
        this.userAccessService = userAccessService;
    }

    public CurrentUserModel getCurrentUserFromSession() {
        Optional<UsernamePasswordAuthenticationToken> optionalUserAuthToken = SecurityContextUtils.retrieveUserAuthToken();
        if (optionalUserAuthToken.isPresent()) {
            UserDetails userDetails = SecurityContextUtils.extractUserDetails(optionalUserAuthToken.get());

            Optional<UserEntity> optionalUser = userRepository.findFirstByEmail(userDetails.getUsername());
            if (optionalUser.isPresent()) {
                UserEntity user = optionalUser.get();
                UserRoleModel role = userAccessService.findRoleByUserId(user.getUserId());
                return new CurrentUserModel(user.getUserId(), user.getFirstName(), user.getLastName(), role);
            } else {
                log.error("The logged in user is not in the database. Username: [{}]", userDetails.getUsername());
            }
        }

        return CurrentUserModel.ANONYMOUS_USER;
    }

    public UserAccountModel getUser(Long userId) throws PortalRestException {
        if (userId == null) {
            throw new PortalRestException(HttpStatus.BAD_REQUEST);
        }

        if (!userAccessService.canCurrentUserAccessDataForUser(userId)) {
            log.warn("There was an attempt to access the user with id [{}], from an unauthorized account", userId);
            throw new PortalRestException(HttpStatus.FORBIDDEN);
        }

        Optional<UserEntity> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent()) {
            UserEntity user = optionalUser.get();
            UserRoleModel role = userAccessService.findRoleByUserId(user.getUserId());
            return new UserAccountModel(user.getFirstName(), user.getLastName(), user.getEmail(), role, user.getIsActive());
        }
        throw new PortalRestException(HttpStatus.NOT_FOUND);
    }

}
