package txrpc.remote.client;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;

public final class HttpClientUtil {

    public static HttpURLConnection open(URL url, Proxy proxy, int connectTimeout) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection(proxy);
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(connectTimeout);
        conn.setUseCaches(false);
        conn.setAllowUserInteraction(false);
        return conn;
    }

    public interface HttpAction<T> {

        T execute() throws IOException;
    }

    public static <T> T runQuery(HttpURLConnection conn, HttpAction<T> action) throws IOException {
        conn.connect();
        try {
            return action.execute();
        } finally {
            conn.disconnect();
        }
    }
}
