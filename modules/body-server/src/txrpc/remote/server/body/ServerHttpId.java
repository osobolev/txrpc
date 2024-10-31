package txrpc.remote.server.body;

import txrpc.remote.server.IServerSessionId;

public final class ServerHttpId {

    public final IServerSessionId sessionId;
    public final Integer transactionId;

    public ServerHttpId(IServerSessionId sessionId, Integer transactionId) {
        this.sessionId = sessionId;
        this.transactionId = transactionId;
    }
}
