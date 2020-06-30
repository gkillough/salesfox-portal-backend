package com.usepipeline.portal.web.customization.icon;

import com.usepipeline.portal.database.account.entity.MembershipEntity;
import com.usepipeline.portal.database.account.entity.UserEntity;
import com.usepipeline.portal.database.customization.icon.CustomIconEntity;
import com.usepipeline.portal.database.customization.icon.CustomIconOwnerEntity;
import com.usepipeline.portal.database.customization.icon.CustomIconOwnerRepository;
import com.usepipeline.portal.web.util.HttpSafeUserMembershipRetrievalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Component
public class CustomIconAccessService {
    private CustomIconOwnerRepository customIconOwnerRepository;
    private HttpSafeUserMembershipRetrievalService membershipRetrievalService;

    @Autowired
    public CustomIconAccessService(CustomIconOwnerRepository customIconOwnerRepository, HttpSafeUserMembershipRetrievalService membershipRetrievalService) {
        this.customIconOwnerRepository = customIconOwnerRepository;
        this.membershipRetrievalService = membershipRetrievalService;
    }

    public void validateImageAccess(CustomIconEntity customIcon) {
        validateImageAccess(customIcon, null);
    }

    public void validateImageAccess(CustomIconEntity customIcon, @Nullable CustomIconOwnerEntity nullableOwner) {
        if (membershipRetrievalService.isAuthenticatedUserPipelineAdmin()) {
            return;
        }

        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        if (membershipRetrievalService.isAuthenticateUserBasicOrPremiumMember()) {
            CustomIconOwnerEntity owner = Optional.ofNullable(nullableOwner)
                    .orElseGet(() -> customIconOwnerRepository.findById(customIcon.getCustomIconId())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN)));
            if (owner.getUserId().equals(loggedInUser.getUserId())) {
                return;
            }
        } else {
            MembershipEntity userMembership = membershipRetrievalService.getMembershipEntity(loggedInUser);
            if (userMembership.getOrganizationAccountId().equals(customIcon.getOrganizationAccountId())) {
                return;
            }
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

}
