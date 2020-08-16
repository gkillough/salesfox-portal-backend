package com.getboostr.portal.rest.api.registration.plan;

import com.getboostr.portal.database.account.entity.RoleEntity;
import com.getboostr.portal.database.account.repository.RoleRepository;
import com.getboostr.portal.rest.api.registration.organization.OrganizationConstants;
import com.getboostr.portal.rest.security.authorization.PortalAuthorityConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RegistrationPlanService {
    private final RoleRepository roleRepository;

    @Autowired
    public RegistrationPlanService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public MultiPlanModel getPlans() {
        List<RegistrationPlanModel> planModels = roleRepository.getBasicAndPremiumRoles()
                .stream()
                .map(this::convertToPlan)
                .collect(Collectors.toList());
        return new MultiPlanModel(planModels);
    }

    private RegistrationPlanModel convertToPlan(RoleEntity role) {
        String planName = role.getRoleLevel().equals(PortalAuthorityConstants.PORTAL_BASIC_USER) ? OrganizationConstants.PLAN_INDIVIDUAL_BASIC_DISPLAY_NAME : OrganizationConstants.PLAN_INDIVIDUAL_PREMIUM_DISPLAY_NAME;
        return new RegistrationPlanModel(planName, role.getRoleLevel(), role.getDescription());
    }

}
