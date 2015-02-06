package research.ufu.datagather.utils;


import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;

import research.ufu.datagather.model.Constants;

public class HTTPSingleton {
    private static HttpManager httpClient;
    public static HttpManager getHttpClient()
    {
        if(httpClient == null) {
            httpClient = new HttpManager();
        }
        return httpClient;
    }
}