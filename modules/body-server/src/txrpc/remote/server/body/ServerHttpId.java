package txrpc.remote.server.body;

import txrpc.remote.server.IServerSessionId;

public final class ServerHttpId {

    public final IServerSessionId sessionId;
    public final String transactionId;

    public ServerHttpId(IServerSessionId sessionId, String transactionId) {
        this.sessionId = sessionId;
        this.transactionId = transactionId;
    }
}
