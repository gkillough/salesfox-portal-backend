package com.getboostr.portal.rest.api.common.page;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageMetadata {
    public static final Integer DEFAULT_OFFSET = 0;
    public static final String DEFAULT_OFFSET_STRING = "0";
    public static final Integer DEFAULT_LIMIT = 100;
    public static final String DEFAULT_LIMIT_STRING = "100";

    private Integer offset;
    private Integer limit;
    private Integer size;
    private Long total;

}
