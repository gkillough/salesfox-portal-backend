package ai.salesfox.integration.common.http;

import ai.salesfox.integration.common.exception.SalesfoxException;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpStatusCodes;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HttpServiceTest {
    @Test
    public void executeGetNoSpecTest() throws SalesfoxException {
        HttpServiceWrapper httpServiceWrapper = HttpServicesFactory.noProxy("https://google.com");
        HttpResponse httpResponse = null;
        try {
            httpResponse = httpServiceWrapper.executeGet("");
            assertEquals(HttpStatusCodes.STATUS_CODE_OK, httpResponse.getStatusCode());
        } catch (SalesfoxException e) {
            fail("Expected no exception to be thrown when connecting to google", e);
        } finally {
            httpServiceWrapper.disconnectResponse(httpResponse);
        }
    }

}
