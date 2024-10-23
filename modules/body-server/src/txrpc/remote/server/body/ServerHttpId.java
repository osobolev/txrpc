package txrpc.remote.server.body;

import txrpc.remote.server.IServerSessionId;

public final class ServerHttpId {

    public final IServerSessionId sessionId;
    public final Long transactionId;

    public ServerHttpId(IServerSessionId sessionId, Long transactionId) {
        this.sessionId = sessionId;
        this.transactionId = transactionId;
    }
}
