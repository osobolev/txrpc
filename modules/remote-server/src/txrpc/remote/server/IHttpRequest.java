package txrpc.remote.server;

import txrpc.remote.common.HttpId;
import txrpc.remote.common.HttpResult;

import java.io.IOException;

public interface IHttpRequest {

    String hostName();

    String newSessionId();

    HttpId session(ServerHttpId id, String sessionId);

    HttpId transaction(ServerHttpId id, long transactionId);

    ServerHttpRequest requestData() throws IOException;

    void write(HttpResult result) throws IOException;

    default void writeError(Throwable error) throws IOException {
        write(new HttpResult(null, error));
    }
}
