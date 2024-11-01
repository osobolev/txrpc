package txrpc.remote.server.body;

import txrpc.remote.common.body.HttpId;
import txrpc.remote.common.body.ISerializer;
import txrpc.remote.server.IServerSessionId;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public final class BodyHttpRequest extends BaseBodyHttpRequest {

    public BodyHttpRequest(ISerializer serializer, String hostName, InputStream in, OutputStream out) {
        super(serializer, hostName, in, out);
    }

    @Override
    public IServerSessionId newSessionId() {
        return SimpleServerSessionId.create(UUID.randomUUID().toString());
    }

    @Override
    protected Object newSessionWireId(IServerSessionId sessionId) {
        return sessionId.getId();
    }

    @Override
    protected IServerSessionId sessionId(Object wireId) {
        HttpId id = (HttpId) wireId;
        return SimpleServerSessionId.create(id.sessionId);
    }

    @Override
    protected String transactionId(Object wireId) {
        HttpId id = (HttpId) wireId;
        return id.transactionId == null ? null : id.transactionId.toString();
    }
}
