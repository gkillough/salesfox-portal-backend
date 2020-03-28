package com.usepipeline.portal.common.model;

import com.usepipeline.portal.database.common.AbstractAddressEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

}
