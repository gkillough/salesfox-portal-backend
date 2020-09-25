package ai.salesfox.integration.scribeless.model;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScribelessErrorResponseModel {
    private Boolean success;
    @SerializedName("error_message")
    private String errorMessage;
    @SerializedName("error_code")
    private String errorCode;

}
