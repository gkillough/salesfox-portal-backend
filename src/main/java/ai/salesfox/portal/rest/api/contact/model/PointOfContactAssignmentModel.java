package ai.salesfox.portal.rest.api.contact.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PointOfContactAssignmentModel {
    private UUID userId;

}
