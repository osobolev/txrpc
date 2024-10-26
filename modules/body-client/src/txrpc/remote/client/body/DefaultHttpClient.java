package txrpc.remote.client.body;

import txrpc.remote.client.HttpClientUtil;
import txrpc.remote.client.IClientSessionId;
import txrpc.remote.common.body.HttpRequest;
import txrpc.remote.common.body.HttpResult;
import txrpc.remote.common.body.ISerializer;
import txrpc.remote.common.body.JavaSerializer;

import java.io.IOException;
import java.net.*;

public final class DefaultHttpClient implements IHttpClient {

    private final URL url;
    private final Proxy proxy;
    private final int connectTimeout;
    private final ISerializer serializer;

    public static final class Builder {

        private final URL url;
        private int connectTimeout = 3000;
        private ISerializer serializer = null;
        private Proxy proxy = Proxy.NO_PROXY;

        public Builder(URL url) {
            this.url = url;
        }

        public Builder setConnectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public Builder setSerializer(ISerializer serializer) {
            this.serializer = serializer;
            return this;
        }

        public Builder setProxy(Proxy proxy) {
            this.proxy = proxy;
            return this;
        }

        public DefaultHttpClient build() {
            return new DefaultHttpClient(url, proxy, connectTimeout, serializer == null ? new JavaSerializer() : serializer);
        }
    }

    public static Builder builder(URL url) {
        return new Builder(url);
    }

    public static Builder builder(String url) throws URISyntaxException, MalformedURLException {
        return builder(new URI(url).normalize().toURL());
    }

    public DefaultHttpClient(URL url, Proxy proxy, int connectTimeout, ISerializer serializer) {
        this.url = url;
        this.proxy = proxy;
        this.connectTimeout = connectTimeout;
        this.serializer = serializer;
    }

    @Override
    public HttpResult call(Class<?> retType, IClientSessionId sessionId, HttpRequest request) throws IOException {
        HttpURLConnection conn = HttpClientUtil.open(url, proxy, connectTimeout);
        return HttpClientUtil.runQuery(conn, () -> {
            try (ISerializer.Writer toServer = serializer.newWriter(conn.getOutputStream())) {
                toServer.write(request, HttpRequest.class);
            }
            try (ISerializer.Reader fromServer = serializer.newReader(conn.getInputStream())) {
                return fromServer.read(HttpResult.class);
            }
        });
    }
}
