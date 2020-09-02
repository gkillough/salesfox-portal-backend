package ai.salesfox.integration.scribeless.service.on_demand;

import ai.salesfox.integration.common.exception.SalesfoxException;
import ai.salesfox.integration.common.http.HttpRequestConfig;
import ai.salesfox.integration.common.http.HttpService;
import ai.salesfox.integration.scribeless.model.ApiKeyHolder;
import com.google.api.client.http.HttpResponse;
import lombok.AllArgsConstructor;
import org.apache.http.entity.ContentType;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.function.BiConsumer;

@AllArgsConstructor
public class OnDemandService {
    public static final String ON_DEMAND_TEXT_ENDPOINT = "/on-demand/text";
    public static final HttpRequestConfig GET_PREVIEW_IMAGE_CONFIG = new HttpRequestConfig(
            10,
            ContentType.APPLICATION_OCTET_STREAM.getMimeType(),
            ContentType.APPLICATION_OCTET_STREAM.getMimeType()
    );

    private final ApiKeyHolder apiKeyHolder;
    private final HttpService httpService;

    // https://us-central1-hc-application-interface-prod.cloudfunctions.net/

    public BufferedImage generatePreviewImage(String text, OnDemandPreviewParams params) throws SalesfoxException {
        StringBuilder requestSpecBuilder = new StringBuilder(ON_DEMAND_TEXT_ENDPOINT);
        appendParam(requestSpecBuilder, '?', ApiKeyHolder.PARAM_NAME_API_KEY, apiKeyHolder.getApiKey());

        BiConsumer<String, String> appender = (paramName, param) -> appendParam(requestSpecBuilder, '&', paramName, param);
        params.getTesting().ifPresent(testing -> appender.accept("testing", testing.toString()));
        params.getWidthInMillimeters().ifPresent(width -> appender.accept("width", width.toString()));
        params.getHeightInMillimeters().ifPresent(height -> appender.accept("height", height.toString()));
        params.getSizeInMillimeters().ifPresent(size -> appender.accept("size", size.toString()));
        params.getFontColor().ifPresent(fontColor -> appender.accept("colour", fontColor));
        params.getHandwritingStyle().ifPresent(style -> appender.accept("style", style));

        HttpResponse response = httpService.executeGet(GET_PREVIEW_IMAGE_CONFIG, requestSpecBuilder.toString());
        try {
            return ImageIO.read(response.getContent());
        } catch (IOException ioException) {
            throw new SalesfoxException(ioException);
        } finally {
            httpService.disconnectResponse(response);
        }
    }

    private void appendParam(StringBuilder paramBuilder, char prefix, CharSequence key, CharSequence value) {
        paramBuilder.append(prefix);
        paramBuilder.append(key);
        paramBuilder.append('=');
        paramBuilder.append(value);
    }

}
