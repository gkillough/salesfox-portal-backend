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
public class MultiInventoryModel extends PagedResponseModel {
    private List<InventoryResponseModel> inventories;

    public static MultiInventoryModel empty() {
        return new MultiInventoryModel(List.of(), Page.empty());
    }

    public MultiInventoryModel(List<InventoryResponseModel> inventories, Page<?> page) {
        super(page);
        this.inventories = inventories;
    }

}
