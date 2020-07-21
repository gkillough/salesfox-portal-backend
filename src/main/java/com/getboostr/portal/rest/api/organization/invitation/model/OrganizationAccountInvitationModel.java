package com.getboostr.portal.rest.api.organization.invitation.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationAccountInvitationModel {
    private String organizationAccountName;
    private String inviteEmail;
    private String inviteRole;

}
