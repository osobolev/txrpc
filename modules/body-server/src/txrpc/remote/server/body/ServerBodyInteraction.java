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
    protected ServerHttpId serverId(HttpId id) {
        return new ServerHttpId(SimpleServerSessionId.create(id.sessionId), id.transactionId);
    }

    @Override
    public IServerSessionId newSessionId() {
        return SimpleServerSessionId.create(UUID.randomUUID().toString());
    }

    @Override
    public HttpId session(ServerHttpId id, IServerSessionId sessionId) {
        return new HttpId(sessionId.getId(), null);
    }

    @Override
    public HttpId transaction(ServerHttpId id, long transactionId) {
        return new HttpId(id.sessionId.getId(), transactionId);
    }
}
