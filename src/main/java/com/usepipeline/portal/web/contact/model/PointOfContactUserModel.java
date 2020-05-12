package com.usepipeline.portal.web.contact.model;

import com.usepipeline.portal.database.account.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PointOfContactUserModel {
    private Long userId;
    private String firstName;
    private String lastName;

    public static PointOfContactUserModel fromUserEntity(UserEntity user) {
        return new PointOfContactUserModel(user.getUserId(), user.getFirstName(), user.getLastName());
    }

}
