package txrpc.remote.server.body;

import txrpc.remote.common.body.HttpRequest;
import txrpc.remote.common.body.HttpResult;
import txrpc.remote.server.IServerSessionId;

import java.io.IOException;

public interface IBodyInteraction {

    IServerSessionId newSessionId();

    IServerSessionId sessionId(Object wireId);

    String transactionId(Object wireId);

    Object sessionWireId(IServerSessionId sessionId);

    HttpRequest requestData() throws IOException;

    void write(HttpResult result) throws IOException;

    default void writeError(Throwable error) throws IOException {
        write(new HttpResult(null, error));
    }
}
