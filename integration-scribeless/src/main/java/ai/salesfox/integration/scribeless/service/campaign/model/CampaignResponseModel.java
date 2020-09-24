package ai.salesfox.integration.scribeless.service.campaign.model;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampaignResponseModel {
    private String id;
    private Boolean success;
    private Integer cost;
    private String status;
    private String currency;
    @SerializedName("stationary_thumbnail")
    private String stationaryThumbnail;

}
