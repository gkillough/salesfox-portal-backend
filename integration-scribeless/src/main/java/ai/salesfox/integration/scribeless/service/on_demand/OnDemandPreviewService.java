package ai.salesfox.integration.scribeless.service.on_demand;

import ai.salesfox.integration.common.exception.SalesfoxException;
import ai.salesfox.integration.common.http.HttpRequestConfig;
import ai.salesfox.integration.common.http.HttpServiceWrapper;
import ai.salesfox.integration.common.http.QueryParamBuilder;
import ai.salesfox.integration.scribeless.model.ApiKeyHolder;
import ai.salesfox.integration.scribeless.service.on_demand.model.OnDemandPreviewParams;
import com.google.api.client.http.HttpResponse;
import lombok.AllArgsConstructor;
import org.apache.http.entity.ContentType;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

@AllArgsConstructor
public class OnDemandPreviewService {
    public static final String ON_DEMAND_TEXT_ENDPOINT = "/api/v2/on-demand/text";
    public static final HttpRequestConfig GET_PREVIEW_IMAGE_CONFIG = new HttpRequestConfig(
            5000,
            ContentType.APPLICATION_OCTET_STREAM.getMimeType(),
            ContentType.APPLICATION_OCTET_STREAM.getMimeType()
    );

    private final boolean testing;
    private final ApiKeyHolder apiKeyHolder;
    private final HttpServiceWrapper httpServiceWrapper;

    public BufferedImage getPreviewImage(String text, OnDemandPreviewParams params) throws SalesfoxException {
        QueryParamBuilder queryParamBuilder = new QueryParamBuilder(ApiKeyHolder.PARAM_NAME_API_KEY, apiKeyHolder.getApiKey());
        queryParamBuilder.appendAdditionalParam("text", text);

        queryParamBuilder.appendAdditionalParam("testing", Boolean.toString(testing));
        params.getWidthInMillimeters().ifPresent(width -> queryParamBuilder.appendAdditionalParam("width", width.toString()));
        params.getHeightInMillimeters().ifPresent(height -> queryParamBuilder.appendAdditionalParam("height", height.toString()));
        params.getSizeInMillimeters().ifPresent(size -> queryParamBuilder.appendAdditionalParam("size", size.toString()));
        params.getFontColor().ifPresent(fontColor -> queryParamBuilder.appendAdditionalParam("colour", fontColor));
        params.getHandwritingStyle().ifPresent(style -> queryParamBuilder.appendAdditionalParam("style", style));

        String requestSpec = ON_DEMAND_TEXT_ENDPOINT + queryParamBuilder.build();
        // TODO improve error handling
        HttpResponse response = httpServiceWrapper.executeGet(GET_PREVIEW_IMAGE_CONFIG, requestSpec);
        try {
            return ImageIO.read(response.getContent());
        } catch (IOException ioException) {
            throw new SalesfoxException(ioException);
        } finally {
            httpServiceWrapper.disconnectResponse(response);
        }
    }

}
