package com.getboostr.portal.common.model;

import com.getboostr.portal.database.common.AbstractAddressEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PortalAddressModel {
    private Integer streetNumber;
    private String streetName;
    private String aptSuite;
    private String city;
    private String state;
    private String zipCode;
    private Boolean isBusiness;

    public static PortalAddressModel fromEntity(AbstractAddressEntity entity) {
        return new PortalAddressModel(
                entity.getStreetNumber(),
                entity.getStreetName(),
                entity.getAptSuite(),
                entity.getCity(),
                entity.getState(),
                entity.getZipCode(),
                entity.getIsBusiness()
        );
    }

    public void copyFieldsToEntity(AbstractAddressEntity entity) {
        entity.setStreetNumber(getStreetNumber());
        entity.setStreetName(getStreetName());
        entity.setAptSuite(getAptSuite());
        entity.setCity(getCity());
        entity.setState(StringUtils.upperCase(getState()));
        entity.setZipCode(getZipCode());
        entity.setIsBusiness(getIsBusiness());
    }

}
