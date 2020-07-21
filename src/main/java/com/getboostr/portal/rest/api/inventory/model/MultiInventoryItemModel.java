package com.getboostr.portal.rest.api.inventory.model;

import com.getboostr.portal.rest.api.common.page.PagedResponseModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MultiInventoryItemModel extends PagedResponseModel {
    private List<InventoryItemResponseModel> items;

    public static MultiInventoryItemModel empty() {
        return new MultiInventoryItemModel(List.of(), Page.empty());
    }

    public MultiInventoryItemModel(List<InventoryItemResponseModel> items, Page<?> page) {
        super(page);
        this.items = items;
    }

}
