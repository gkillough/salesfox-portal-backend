package ai.salesfox.integration.scribeless.service.campaign.model;

import ai.salesfox.integration.scribeless.model.ScribelessAddressModel;
import ai.salesfox.integration.scribeless.model.ScribelessRequestDeliveryModel;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampaignCreationRequestModel {
    public static final String SCRIBELESS_DATE_FORMAT = "dd/MM/yyy";

    @SerializedName("paper_size")
    private String paperSize;
    @SerializedName("handwriting_style")
    private String handwritingStyle;
    @SerializedName("handwriting_colour")
    private String handwritingColour;
    @SerializedName("handwriting_size")
    private String handwritingSize;
    private String title;
    private String product;
    private String text;
    private String inserts;
    @SerializedName("due_date")
    private String dueDate;
    private String notes;
    @SerializedName("header_image")
    private String headerImage;
    @SerializedName("header_type")
    private String headerType;
    @SerializedName("header_font")
    private String headerFont;
    @SerializedName("header_text")
    private String headerText;
    @SerializedName("footer_text")
    private String footerText;
    @SerializedName("footer_font")
    private String footerFont;
    // TODO determine what this does
    private ScribelessRequestDeliveryModel delivery;
    @SerializedName("return_address")
    private ScribelessAddressModel returnAddress;
    private List<ScribelessAddressModel> recipients;

}
