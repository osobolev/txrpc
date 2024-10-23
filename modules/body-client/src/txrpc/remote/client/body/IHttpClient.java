package txrpc.remote.client.body;

import txrpc.remote.client.IClientSessionId;
import txrpc.remote.common.body.HttpRequest;
import txrpc.remote.common.body.HttpResult;

import java.io.IOException;

public interface IHttpClient {

    HttpResult call(Class<?> retType, IClientSessionId sessionId, HttpRequest request) throws IOException;
}
