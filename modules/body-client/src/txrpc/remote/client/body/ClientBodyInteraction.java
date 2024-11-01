package txrpc.remote.client.body;

import txrpc.remote.client.IClientSessionId;
import txrpc.remote.common.body.HttpId;

public final class ClientBodyInteraction extends BaseClientBodyInteraction {

    public ClientBodyInteraction(IHttpClient client) {
        super(client);
    }

    @Override
    protected IClientSessionId newSessionId() {
        return null;
    }

    @Override
    protected IClientSessionId newSessionId(IClientSessionId sessionId, Object newSessionWireId) {
        String id = (String) newSessionWireId;
        return new SimpleClientSessionId(id);
    }

    @Override
    protected HttpId wireId(IClientSessionId sessionId, String transactionId) {
        SimpleClientSessionId id = (SimpleClientSessionId) sessionId;
        return new HttpId(
            id == null ? null : id.id,
            transactionId == null ? null : Integer.valueOf(transactionId)
        );
    }
}
