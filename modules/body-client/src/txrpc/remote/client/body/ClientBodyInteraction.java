package txrpc.remote.client.body;

import txrpc.remote.client.IClientSessionId;
import txrpc.remote.common.body.HttpDBInterfaceInfo;
import txrpc.remote.common.body.HttpId;

public final class ClientBodyInteraction extends BaseClientBodyInteraction {

    public ClientBodyInteraction(IHttpClient client) {
        super(client);
    }

    protected IClientSessionId newSessionId() {
        return null;
    }

    protected IClientSessionId newSessionId(IClientSessionId sessionId, HttpDBInterfaceInfo info) {
        return new SimpleClientSessionId(info.id.sessionId);
    }

    protected HttpId id(IClientSessionId sessionId, String transactionId) {
        SimpleClientSessionId id = (SimpleClientSessionId) sessionId;
        return new HttpId(id == null ? null : id.id, transactionId == null ? null : Integer.valueOf(transactionId));
    }
}
