package txrpc.remote.client;

import txrpc.remote.common.HttpRequest;
import txrpc.remote.common.HttpResult;

import java.io.IOException;

public interface IHttpClient {

    Object newContext();

    HttpResult call(Class<?> retType, Object clientContext, HttpRequest request) throws IOException;
}
