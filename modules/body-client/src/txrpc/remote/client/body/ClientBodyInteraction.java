package txrpc.remote.client.body;

import txrpc.remote.client.IClientSessionId;
import txrpc.remote.common.body.HttpId;

public final class ClientBodyInteraction extends BaseClientBodyInteraction {

    public ClientBodyInteraction(IHttpClient client) {
        super(client);
    }

    protected IClientSessionId newSessionId() {
        return null;
    }

    protected IClientSessionId newSessionId(IClientSessionId sessionId, Object wireId) {
        HttpId id = (HttpId) wireId;
        return new SimpleClientSessionId(id.sessionId);
    }

    protected Object wireId(IClientSessionId sessionId, String transactionId) {
        SimpleClientSessionId id = (SimpleClientSessionId) sessionId;
        return new HttpId(
            id == null ? null : id.id,
            transactionId == null ? null : Integer.valueOf(transactionId)
        );
    }
}
