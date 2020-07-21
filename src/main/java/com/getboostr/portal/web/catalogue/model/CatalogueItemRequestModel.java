package com.getboostr.portal.web.catalogue.model;

import com.getboostr.portal.web.common.model.request.RestrictionModel;
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
