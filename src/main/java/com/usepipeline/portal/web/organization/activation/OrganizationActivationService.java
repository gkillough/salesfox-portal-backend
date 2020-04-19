package com.usepipeline.portal.web.organization.activation;

import com.usepipeline.portal.web.common.model.ActiveStatusPatchModel;
import com.usepipeline.portal.web.util.HttpSafeUserMembershipRetrievalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrganizationActivationService {
    private HttpSafeUserMembershipRetrievalService membershipRetrievalService;

    @Autowired
    public OrganizationActivationService(HttpSafeUserMembershipRetrievalService membershipRetrievalService) {
        this.membershipRetrievalService = membershipRetrievalService;
    }

    public void updateOrganizationAccountActiveStatus(Long organizationAccountId, ActiveStatusPatchModel activeStatusPatchModel) {
        // Restrict to organization account owners and pipeline admins

        // Change license active status

        // Change active status user accounts for organization account

    }

}
