package com.usepipeline.portal.web.common.page;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class PagedResponseModel {
    private PageMetadata meta;

    public PagedResponseModel(Page<?> page) {
        this.meta = new PageMetadata(page.getPageable().getPageNumber(), page.getSize(), page.getNumberOfElements(), page.getTotalElements());
    }

}
