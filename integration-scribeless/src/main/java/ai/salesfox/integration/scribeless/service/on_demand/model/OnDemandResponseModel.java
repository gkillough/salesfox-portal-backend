package ai.salesfox.integration.scribeless.service.on_demand.model;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OnDemandResponseModel {
    @SerializedName("campaign_id")
    private String campaignId;
    private Boolean success;
    @SerializedName("campaign_status")
    private String campaignStatus;
    @SerializedName("stationary_thumbnail")
    private String stationaryThumbnail;

}
