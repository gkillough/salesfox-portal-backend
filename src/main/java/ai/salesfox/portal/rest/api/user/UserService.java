package ai.salesfox.portal.rest.api.user;

import ai.salesfox.portal.database.account.entity.MembershipEntity;
import ai.salesfox.portal.database.account.entity.RoleEntity;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.account.repository.UserRepository;
import ai.salesfox.portal.rest.api.common.page.PageRequestValidationUtils;
import ai.salesfox.portal.rest.api.user.common.UserAccessService;
import ai.salesfox.portal.rest.api.user.common.model.CurrentUserModel;
import ai.salesfox.portal.rest.api.user.common.model.MultiUserModel;
import ai.salesfox.portal.rest.api.user.common.model.UserAccountModel;
import ai.salesfox.portal.rest.api.user.common.model.UserSummaryModel;
import ai.salesfox.portal.rest.api.user.role.model.UserRoleModel;
import ai.salesfox.portal.rest.util.HttpSafeUserMembershipRetrievalService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

    public MultiUserModel getUsers(Integer pageOffset, Integer pageLimit, String query) {
        PageRequestValidationUtils.validatePagingParams(pageOffset, pageLimit);
        PageRequest pageRequest = PageRequest.of(pageOffset, pageLimit);

        Page<UserEntity> foundUsers;
        if (StringUtils.isNotBlank(query)) {
            foundUsers = userRepository.findByQuery(query, pageRequest);
        } else {
            foundUsers = userRepository.findAll(pageRequest);
        }

        if (foundUsers.isEmpty()) {
            return MultiUserModel.empty();
        }

        List<UserSummaryModel> userSummaries = foundUsers
                .stream()
                .map(userEntity -> new UserSummaryModel(userEntity.getUserId(), userEntity.getFirstName(), userEntity.getLastName(), userEntity.getEmail()))
                .collect(Collectors.toList());
        return new MultiUserModel(userSummaries, foundUsers);
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
