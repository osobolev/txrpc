package txrpc.remote.server;

public final class ServerHttpId {

    public final String sessionId;
    public final Long transactionId;

    public ServerHttpId(String sessionId, Long transactionId) {
        this.sessionId = sessionId;
        this.transactionId = transactionId;
    }
}
