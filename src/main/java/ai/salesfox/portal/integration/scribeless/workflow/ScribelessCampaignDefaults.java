package ai.salesfox.portal.integration.scribeless.workflow;

import ai.salesfox.integration.scribeless.enumeration.ScribelessPaperSize;
import ai.salesfox.integration.scribeless.model.ScribelessRequestDeliveryModel;

public class ScribelessCampaignDefaults {
    public static final String DEFAULT_WHITESPACE_CHAR = "\n";
    public static final String DEFAULT_COUNTRY = "USA";
    public static final String DEFAULT_PAPER_SIZE_USA = ScribelessPaperSize.POSTCARD_LANDSCAPE.getValue();
    public static final String DEFAULT_HEADER_TYPE = "Logo";
    public static final String DEFAULT_FOOTER_FONT = "Josefin Sans";
    public static final ScribelessRequestDeliveryModel DEFAULT_DELIVERY_MODEL = new ScribelessRequestDeliveryModel("Print on Request");

}
