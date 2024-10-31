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

    protected IClientSessionId newSessionId(IClientSessionId sessionId, HttpId id) {
        return new SimpleClientSessionId(id.sessionId);
    }

    protected HttpId wireId(IClientSessionId sessionId, String transactionId) {
        SimpleClientSessionId id = (SimpleClientSessionId) sessionId;
        return HttpId.create(id == null ? null : id.id, transactionId);
    }
}
