package ai.salesfox.integration.common.http;

import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.apache.v2.ApacheHttpTransport;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;

import java.net.Proxy;

public class HttpServicesFactory {
    private static final Gson DEFAULT_GSON = new GsonBuilder().create();

    public static HttpServiceWrapper noProxy(String baseUrl) {
        return withProxy(baseUrl, Proxy.NO_PROXY);
    }

    public static HttpServiceWrapper withProxy(String baseUrl, Proxy proxy) {
        return new HttpServiceWrapper(baseUrl, defaultGson(), requestFactoryWithProxy(proxy));
    }

    public static Gson defaultGson() {
        return DEFAULT_GSON;
    }

    public static HttpRequestFactory requestFactoryWithProxy(Proxy proxy) {
        HttpTransport httpTransport = transportWithProxy(proxy);
        return httpTransport.createRequestFactory();
    }

    public static HttpTransport transportWithProxy(Proxy proxy) {
        HttpClient httpClient = clientWithProxy(proxy);
        return new ApacheHttpTransport(httpClient);
    }

    public static HttpClient clientWithProxy(Proxy proxy) {
        HttpHost poxyHost = HttpHost.create(proxy.toString());
        return ApacheHttpTransport.newDefaultHttpClientBuilder()
                .setProxy(poxyHost)
                .build();
    }

}
