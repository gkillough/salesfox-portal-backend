package com.usepipeline.portal.web.user.active;

import com.usepipeline.portal.database.account.entity.MembershipEntity;
import com.usepipeline.portal.database.account.entity.UserEntity;
import com.usepipeline.portal.database.account.repository.MembershipRepository;
import com.usepipeline.portal.database.account.repository.UserRepository;
import com.usepipeline.portal.web.common.model.ActiveStatusUpdateModel;
import com.usepipeline.portal.web.user.common.UserAccessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;

@Component
public class UserActiveService {
    private UserAccessService userAccessService;
    private UserRepository userRepository;
    private MembershipRepository membershipRepository;

    @Autowired
    public UserActiveService(UserAccessService userAccessService, UserRepository userRepository, MembershipRepository membershipRepository) {
        this.userAccessService = userAccessService;
        this.userRepository = userRepository;
        this.membershipRepository = membershipRepository;
    }

    @Transactional
    public void updateUserActiveStatus(Long userId, ActiveStatusUpdateModel updateModel) {
        if (!userAccessService.canCurrentUserAccessDataForUser(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        if (updateModel.getActiveStatus() == null) {
            throw createBadRequest("The field 'activeStatus' cannot be null");
        }

        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> createBadRequest(String.format("A user with the id ['%s'] does not exist", userId)));
        userEntity.setIsActive(updateModel.getActiveStatus());
        userRepository.save(userEntity);

        MembershipEntity membershipEntity = membershipRepository.findFirstByUserId(userId)
                .orElseThrow(() -> createBadRequest(String.format("A membership with the user id ['%s'] does not exist", userId)));
        membershipEntity.setIsActive(updateModel.getActiveStatus());
        membershipRepository.save(membershipEntity);
    }

    private ResponseStatusException createBadRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

}
