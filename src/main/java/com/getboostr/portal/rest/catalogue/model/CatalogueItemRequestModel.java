package com.getboostr.portal.rest.catalogue.model;

import com.getboostr.portal.rest.common.model.request.RestrictionModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CatalogueItemRequestModel {
    private String name;
    private BigDecimal price;
    private Long quantity;
    private RestrictionModel restriction;

}
