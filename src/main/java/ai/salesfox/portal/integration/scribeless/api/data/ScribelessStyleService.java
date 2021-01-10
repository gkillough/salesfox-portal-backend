package ai.salesfox.portal.integration.scribeless.api.data;

import ai.salesfox.integration.common.util.SalesfoxEnumUtils;
import ai.salesfox.integration.scribeless.enumeration.ScribelessFontColor;
import ai.salesfox.integration.scribeless.enumeration.ScribelessFontSize;
import ai.salesfox.integration.scribeless.enumeration.ScribelessHandwritingStyles;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class ScribelessStyleService {
    public ScribelessStyleOptionsResponseModel getScribelessStyleOptions() {
        String[] fontColors = SalesfoxEnumUtils.capitalizeValues(ScribelessFontColor.values());
        String[] fontSizes = SalesfoxEnumUtils.capitalizeValues(ScribelessFontSize.values());
        String[] handwritingStyles = SalesfoxEnumUtils.capitalizeValues(ScribelessHandwritingStyles.values());
        return new ScribelessStyleOptionsResponseModel(Arrays.asList(fontColors), Arrays.asList(fontSizes), Arrays.asList(handwritingStyles));
    }

}
