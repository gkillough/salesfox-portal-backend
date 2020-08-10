package com.getboostr.portal.rest.api.user.common.model;

import com.getboostr.portal.database.account.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryModel {
    private UUID userId;
    private String firstName;
    private String lastName;
    private String email;

    public static UserSummaryModel fromEntity(UserEntity userEntity) {
        return new UserSummaryModel(userEntity.getUserId(), userEntity.getFirstName(), userEntity.getLastName(), userEntity.getEmail());
    }

}
