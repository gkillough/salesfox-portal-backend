package ai.salesfox.portal.rest.api.user;

import ai.salesfox.portal.database.account.entity.MembershipEntity;
import ai.salesfox.portal.database.account.entity.RoleEntity;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.account.repository.UserRepository;
import ai.salesfox.portal.rest.api.user.common.UserAccessService;
import ai.salesfox.portal.rest.api.user.common.model.CurrentUserModel;
import ai.salesfox.portal.rest.api.user.common.model.UserAccountModel;
import ai.salesfox.portal.rest.api.user.role.model.UserRoleModel;
import ai.salesfox.portal.rest.util.HttpSafeUserMembershipRetrievalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Slf4j
@Component
public class UserService {
    private final UserRepository userRepository;
    private final UserAccessService userAccessService;
    private final HttpSafeUserMembershipRetrievalService userMembershipRetrievalService;

    @Autowired
    public UserService(UserRepository userRepository, UserAccessService userAccessService, HttpSafeUserMembershipRetrievalService userMembershipRetrievalService) {
        this.userRepository = userRepository;
        this.userAccessService = userAccessService;
        this.userMembershipRetrievalService = userMembershipRetrievalService;
    }

    public CurrentUserModel getCurrentUserFromSession() {
        UserEntity user = userMembershipRetrievalService.getAuthenticatedUserEntity();
        UserRoleModel userRoleModel = createRoleModel(user);
        return new CurrentUserModel(user.getUserId(), user.getFirstName(), user.getLastName(), userRoleModel);
    }

    public UserAccountModel getUser(UUID userId) {
        UserEntity foundUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        UserEntity loggedInUser = userMembershipRetrievalService.getAuthenticatedUserEntity();
        if (!userAccessService.canUserAccessDataForUser(loggedInUser, foundUser)) {
            log.warn("There was an attempt to access the user with id [{}], from an unauthorized account", userId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        UserRoleModel role = createRoleModel(foundUser);
        return new UserAccountModel(foundUser.getFirstName(), foundUser.getLastName(), foundUser.getEmail(), role, foundUser.getIsActive());
    }

    private UserRoleModel createRoleModel(UserEntity user) {
        MembershipEntity userMembership = user.getMembershipEntity();
        RoleEntity role = userMembership.getRoleEntity();
        return new UserRoleModel(role.getRoleLevel(), role.getDescription());
    }

}
