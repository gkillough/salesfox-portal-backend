package ai.salesfox.portal.common.model;

import ai.salesfox.portal.database.common.AbstractAddressEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PortalAddressModel {
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String zipCode;
    private Boolean isBusiness;

    public static PortalAddressModel fromEntity(AbstractAddressEntity entity) {
        return new PortalAddressModel(
                entity.getAddressLine1(),
                entity.getAddressLine2(),
                entity.getCity(),
                entity.getState(),
                entity.getZipCode(),
                entity.getIsBusiness()
        );
    }

    public void copyFieldsToEntity(AbstractAddressEntity entity) {
        entity.setAddressLine1(getAddressLine1());
        entity.setAddressLine2(getAddressLine2());
        entity.setCity(getCity());
        entity.setState(StringUtils.upperCase(getState()));
        entity.setZipCode(getZipCode());
        entity.setIsBusiness(getIsBusiness());
    }

}
