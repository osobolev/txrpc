package txrpc.remote.server.body;

import txrpc.remote.common.body.HttpId;
import txrpc.remote.common.body.HttpRequest;
import txrpc.remote.common.body.HttpResult;
import txrpc.remote.server.IServerSessionId;

import java.io.IOException;

public interface IBodyInteraction {

    IServerSessionId newSessionId();

    IServerSessionId sessionId(HttpId id);

    String transactionId(HttpId id);

    HttpId sessionWireId(IServerSessionId sessionId);

    HttpRequest requestData() throws IOException;

    void write(HttpResult result) throws IOException;

    default void writeError(Throwable error) throws IOException {
        write(new HttpResult(null, error));
    }
}
