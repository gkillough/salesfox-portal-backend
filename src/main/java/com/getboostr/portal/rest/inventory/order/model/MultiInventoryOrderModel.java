package com.getboostr.portal.rest.inventory.order.model;

import com.getboostr.portal.rest.common.page.PagedResponseModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MultiInventoryOrderModel extends PagedResponseModel {
    private List<InventoryOrderResponseModel> orders;

    public static MultiInventoryOrderModel empty() {
        return new MultiInventoryOrderModel(List.of(), Page.empty());
    }

    public MultiInventoryOrderModel(List<InventoryOrderResponseModel> orders, Page<?> page) {
        super(page);
        this.orders = orders;
    }

}
