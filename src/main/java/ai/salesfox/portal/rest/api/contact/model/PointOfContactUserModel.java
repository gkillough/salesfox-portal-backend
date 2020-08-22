package ai.salesfox.portal.rest.api.contact.model;

import ai.salesfox.portal.database.account.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PointOfContactUserModel {
    private UUID userId;
    private String firstName;
    private String lastName;

    public static PointOfContactUserModel fromUserEntity(UserEntity user) {
        return new PointOfContactUserModel(user.getUserId(), user.getFirstName(), user.getLastName());
    }

}
