package com.usepipeline.portal.web.customization.icon.model;

import com.usepipeline.portal.web.common.page.PagedResponseModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MultiCustomIconResponseModel extends PagedResponseModel {
    private List<CustomIconResponseModel> icons;

    public static MultiCustomIconResponseModel empty() {
        return new MultiCustomIconResponseModel(List.of(), Page.empty());
    }

    public MultiCustomIconResponseModel(List<CustomIconResponseModel> icons, Page<?> page) {
        super(page);
        this.icons = icons;
    }

}
