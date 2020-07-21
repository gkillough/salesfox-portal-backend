package com.getboostr.portal.database.organization.account.invite;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationAccountInviteTokenPK implements Serializable {
    private String email;
    private String token;

}
