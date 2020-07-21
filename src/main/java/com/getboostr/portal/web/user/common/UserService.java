package com.getboostr.portal.web.user.common;

import com.getboostr.portal.database.account.entity.UserEntity;
import com.getboostr.portal.database.account.repository.UserRepository;
import com.getboostr.portal.web.user.common.model.CurrentUserModel;
import com.getboostr.portal.web.user.common.model.UserAccountModel;
import com.getboostr.portal.web.util.HttpSafeUserMembershipRetrievalService;
import com.getboostr.portal.web.user.role.model.UserRoleModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class UserService {
    private UserRepository userRepository;
    private UserAccessService userAccessService;
    private HttpSafeUserMembershipRetrievalService userMembershipRetrievalService;

    @Autowired
    public UserService(UserRepository userRepository, UserAccessService userAccessService, HttpSafeUserMembershipRetrievalService userMembershipRetrievalService) {
        this.userRepository = userRepository;
        this.userAccessService = userAccessService;
        this.userMembershipRetrievalService = userMembershipRetrievalService;
    }

    public CurrentUserModel getCurrentUserFromSession() {
        UserEntity user = userMembershipRetrievalService.getAuthenticatedUserEntity();
        UserRoleModel role = userAccessService.findRoleByUserId(user.getUserId());
        return new CurrentUserModel(user.getUserId(), user.getFirstName(), user.getLastName(), role);
    }

    public UserAccountModel getUser(UUID userId) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The field 'userId' is required");
        }

        if (!userAccessService.canCurrentUserAccessDataForUser(userId)) {
            log.warn("There was an attempt to access the user with id [{}], from an unauthorized account", userId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        Optional<UserEntity> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent()) {
            UserEntity user = optionalUser.get();
            UserRoleModel role = userAccessService.findRoleByUserId(user.getUserId());
            return new UserAccountModel(user.getFirstName(), user.getLastName(), user.getEmail(), role, user.getIsActive());
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

}
