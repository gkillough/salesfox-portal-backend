package ai.salesfox.integration.scribeless.service.campaign.model;

import ai.salesfox.integration.scribeless.model.ScribelessAddressModel;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
// TODO create builder with defaults
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
    // TODO add "delivery" field (object - "delivery: {"sender": "STRING"})
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
    @SerializedName("return_address")
    private ScribelessAddressModel returnAddress;
    private List<ScribelessAddressModel> recipients;

    /* FIXME this is the only missing field (as far as can be determined)
    {"delivery":{"sender":"Print on Request"}}
     */

}
