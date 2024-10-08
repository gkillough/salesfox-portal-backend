package ai.salesfox.portal.rest.api.catalogue.model;

import ai.salesfox.portal.rest.api.common.page.PagedResponseModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MultiCatalogueItemModel extends PagedResponseModel {
    private List<CatalogueItemResponseModel> items;

    public static MultiCatalogueItemModel empty() {
        return new MultiCatalogueItemModel(List.of(), Page.empty());
    }

    public MultiCatalogueItemModel(List<CatalogueItemResponseModel> items, Page<?> page) {
        super(page);
        this.items = items;
    }

}
