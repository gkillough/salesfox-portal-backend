package ai.salesfox.portal.integration.scribeless.api.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScribelessStyleOptionsResponseModel {
    private List<String> fontColors;
    private List<String> fontSizes;
    private List<String> handwritingStyles;

}
