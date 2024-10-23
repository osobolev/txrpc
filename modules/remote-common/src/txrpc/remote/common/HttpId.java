package txrpc.remote.common;

import java.io.Serializable;

public final class HttpId implements Serializable {

    public final String sessionId;
    public final Long transactionId;

    public HttpId() {
        this(null, null);
    }

    public HttpId(String sessionId, Long transactionId) {
        this.sessionId = sessionId;
        this.transactionId = transactionId;
    }
}
