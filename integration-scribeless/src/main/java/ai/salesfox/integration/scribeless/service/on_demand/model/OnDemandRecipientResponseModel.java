package ai.salesfox.integration.scribeless.service.on_demand.model;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OnDemandRecipientResponseModel extends OnDemandResponseModel {
    @SerializedName("recipient_id")
    private String recipientId;
    @SerializedName("recipient_status")
    private String recipientStatus;

}
