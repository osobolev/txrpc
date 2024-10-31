package txrpc.remote.common.body;

import java.io.Serializable;

public final class HttpId implements Serializable {

    public final String sessionId;
    public final Integer transactionId;

    public HttpId() {
        this.sessionId = null;
        this.transactionId = null;
    }

    public HttpId(String sessionId, Integer transactionId) {
        this.sessionId = sessionId;
        this.transactionId = transactionId;
    }
}
