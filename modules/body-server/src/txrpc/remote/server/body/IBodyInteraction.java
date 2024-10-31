package txrpc.remote.server.body;

import txrpc.remote.common.body.HttpId;
import txrpc.remote.common.body.HttpResult;
import txrpc.remote.server.IServerSessionId;

import java.io.IOException;

public interface IBodyInteraction {

    IServerSessionId newSessionId();

    IServerSessionId sessionId(ServerHttpId id);

    String transactionId(ServerHttpId id);

    HttpId sessionWireId(IServerSessionId sessionId);

    ServerHttpRequest requestData() throws IOException;

    void write(HttpResult result) throws IOException;

    default void writeError(Throwable error) throws IOException {
        write(new HttpResult(null, error));
    }
}
