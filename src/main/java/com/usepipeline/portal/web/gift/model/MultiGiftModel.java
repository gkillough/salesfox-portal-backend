package com.usepipeline.portal.web.gift.model;

import com.usepipeline.portal.web.common.page.PagedResponseModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MultiGiftModel extends PagedResponseModel {
    private List<GiftResponseModel> gifts;

    public static MultiGiftModel empty() {
        return new MultiGiftModel(List.of(), Page.empty());
    }

    public MultiGiftModel(List<GiftResponseModel> gifts, Page<?> page) {
        super(page);
        this.gifts = gifts;
    }

}
