package txrpc.remote.client.body;

import txrpc.remote.client.HttpClientUtil;
import txrpc.remote.client.IClientSessionId;
import txrpc.remote.common.RemoteException;
import txrpc.remote.common.body.HttpRequest;
import txrpc.remote.common.body.HttpResult;
import txrpc.remote.common.body.ISerializer;
import txrpc.remote.common.body.JavaSerializer;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.*;
import java.util.function.Consumer;

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

    public static Object[] getStreams(HttpRequest request) {
        if (request.streamIndexes != null) {
            Object[] streams = new Object[request.streamIndexes.length];
            for (int streamIndex : request.streamIndexes) {
                streams[streamIndex] = request.params[streamIndex];
                request.params[streamIndex] = null;
            }
            return streams;
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static void readStreams(ISerializer.Reader fromServer, Method method, Object[] streams) throws IOException {
        if (streams == null)
            return;
        int lastIndex = -1;
        Class<Object> itemType = null;
        Consumer<Object> consumer = null;
        while (true) {
            int streamIndex = fromServer.readStreamIndex();
            if (streamIndex < 0)
                break;
            if (streamIndex != lastIndex) {
                itemType = HttpRequest.getStreamItemType(method, streamIndex);
                if (itemType == null) {
                    throw new RemoteException("Parameter " + streamIndex + " must be Consumer");
                }
                consumer = (Consumer<Object>) streams[streamIndex];
                lastIndex = streamIndex;
            }
            Object item = fromServer.read(itemType);
            consumer.accept(item);
        }
    }

    @Override
    public HttpResult call(Class<?> retType, IClientSessionId sessionId,
                           Method method, HttpRequest request) throws IOException {
        HttpURLConnection conn = HttpClientUtil.open(url, proxy, connectTimeout);
        return HttpClientUtil.runQuery(conn, () -> {
            Object[] streams = getStreams(request);
            try (ISerializer.Writer toServer = serializer.newWriter(conn.getOutputStream())) {
                toServer.write(request, HttpRequest.class);
            }
            try (ISerializer.Reader fromServer = serializer.newReader(conn.getInputStream())) {
                readStreams(fromServer, method, streams);
                return fromServer.read(HttpResult.class);
            }
        });
    }
}
