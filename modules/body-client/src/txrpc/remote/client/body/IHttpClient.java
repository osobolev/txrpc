package txrpc.remote.client.body;

import txrpc.remote.client.IClientSessionId;
import txrpc.remote.common.body.HttpRequest;
import txrpc.remote.common.body.HttpResult;

import java.io.IOException;
import java.lang.reflect.Method;

public interface IHttpClient {

    HttpResult call(Class<?> retType, IClientSessionId sessionId,
                    Method method, HttpRequest request) throws IOException;
}
