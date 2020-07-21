package com.getboostr.portal.rest.api.common.page;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class PagedResponseModel {
    private PageMetadata meta;

    public PagedResponseModel(Page<?> page) {
        Pageable pageable = page.getPageable();
        if (pageable.isPaged()) {
            this.meta = new PageMetadata(pageable.getPageNumber(), page.getSize(), page.getNumberOfElements(), page.getTotalElements());
        } else {
            this.meta = new PageMetadata(0, 0, 0, 0L);
        }
    }

}
