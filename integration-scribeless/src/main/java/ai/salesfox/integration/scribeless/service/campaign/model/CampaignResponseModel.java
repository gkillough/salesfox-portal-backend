package ai.salesfox.integration.scribeless.service.campaign.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampaignResponseModel {
    /*
    {
   "success": true,
   "cost": 378,
   "status": "Pending",
   "id": "6AYveslBNhO3Qlcuq7JI",
   "currency": "usd",
   "stationary_thumbnail": "https://storage.googleapis.com/hc-application-interface-dev.appspot.com/stationary/JhciAEGt59RomVnVdTdE/thumbnail.jpg"
}
     */
    private String id;
    private Boolean success;
    private Integer cost;
    private String status;
    private String currency;
    private String stationary_thumbnail;

}
