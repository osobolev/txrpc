package txrpc.remote.client;

import txrpc.remote.common.HttpRequest;
import txrpc.remote.common.HttpResult;

import java.io.IOException;
import java.lang.reflect.Type;

public interface IHttpClient {

    Object newContext();

    HttpResult call(Type retType, Object clientContext, HttpRequest request) throws IOException;
}
