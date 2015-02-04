package research.ufu.datagather;


import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

public class HTTPSingleton {
    private static HttpClient httpClient;

    public static HttpClient getHttpClient()
    {
        if(httpClient == null)
            httpClient = new DefaultHttpClient();
        return httpClient;
    }
}