package ai.salesfox.portal.rest.api.catalogue.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CatalogueItemExternalDetailsModel {
    private String distributor;
    private String externalId;

}
