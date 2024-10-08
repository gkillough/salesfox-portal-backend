package ai.salesfox.portal.rest.api.customization.branding_text.model;

import ai.salesfox.portal.rest.api.common.page.PagedResponseModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MultiCustomBrandingTextModel extends PagedResponseModel {
    private List<CustomBrandingTextResponseModel> customBrandingTexts;

    public static MultiCustomBrandingTextModel empty() {
        return new MultiCustomBrandingTextModel(List.of(), Page.empty());
    }

    public MultiCustomBrandingTextModel(List<CustomBrandingTextResponseModel> customBrandingTexts, Page<?> page) {
        super(page);
        this.customBrandingTexts = customBrandingTexts;
    }

}
