package txrpc.remote.server.body;

import txrpc.remote.common.body.HttpId;
import txrpc.remote.common.body.ISerializer;
import txrpc.remote.server.IServerSessionId;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public final class ServerBodyInteraction extends BaseServerBodyInteraction {

    public ServerBodyInteraction(ISerializer serializer, InputStream in, OutputStream out) {
        super(serializer, in, out);
    }

    @Override
    public IServerSessionId newSessionId() {
        return SimpleServerSessionId.create(UUID.randomUUID().toString());
    }

    @Override
    public IServerSessionId sessionId(HttpId id) {
        return SimpleServerSessionId.create(id.sessionId);
    }

    @Override
    public String transactionId(HttpId id) {
        return id.transactionId == null ? null : id.transactionId.toString();
    }

    @Override
    public HttpId sessionWireId(IServerSessionId sessionId) {
        return new HttpId(sessionId.getId(), null);
    }
}
