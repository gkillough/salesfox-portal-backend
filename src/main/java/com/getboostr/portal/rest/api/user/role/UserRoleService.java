package com.getboostr.portal.rest.api.user.role;

import com.getboostr.portal.database.account.entity.MembershipEntity;
import com.getboostr.portal.database.account.entity.RoleEntity;
import com.getboostr.portal.database.account.repository.MembershipRepository;
import com.getboostr.portal.database.account.repository.RoleRepository;
import com.getboostr.portal.rest.api.user.role.model.UserRoleUpdateModel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.util.UUID;

@Component
public class UserRoleService {
    private RoleRepository roleRepository;
    private MembershipRepository membershipRepository;

    @Autowired
    public UserRoleService(RoleRepository roleRepository, MembershipRepository membershipRepository) {
        this.roleRepository = roleRepository;
        this.membershipRepository = membershipRepository;
    }

    @Transactional
    public void updateRole(UUID userId, UserRoleUpdateModel updateModel) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        if (StringUtils.isBlank(updateModel.getLevel())) {
            throw createBadRequest("The field 'level' cannot be blank");
        }

        MembershipEntity membershipEntity = membershipRepository.findFirstByUserId(userId)
                .orElseThrow(() -> createBadRequest(String.format("A membership with the user id ['%s'] does not exist", userId)));

        RoleEntity roleEntity = roleRepository.findFirstByRoleLevel(updateModel.getLevel())
                .orElseThrow(() -> createBadRequest(String.format("The role ['%s'] does not exist", updateModel.getLevel())));

        membershipEntity.setRoleId(roleEntity.getRoleId());
        membershipRepository.save(membershipEntity);
    }

    private ResponseStatusException createBadRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

}
