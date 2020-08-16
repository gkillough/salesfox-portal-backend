package com.getboostr.portal.rest.api.registration.plan;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MultiPlanModel {
    private List<RegistrationPlanModel> plans;

}
