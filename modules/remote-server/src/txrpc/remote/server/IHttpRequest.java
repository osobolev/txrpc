package txrpc.remote.server;

import txrpc.remote.common.TxRpcInteraction;

import java.io.IOException;

public interface IHttpRequest {

    String hostName();

    IServerSessionId newSessionId();

    void perform(TxRpcInteraction<IServerSessionId> interaction) throws IOException;

    void writeError(Exception error) throws IOException;
}
