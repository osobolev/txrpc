package txrpc.remote.server;

import txrpc.remote.common.HttpId;
import txrpc.remote.common.ISerializer;

import java.io.InputStream;
import java.io.OutputStream;

public final class BodyHttpRequest extends BaseBodyHttpRequest {

    public BodyHttpRequest(ISerializer serializer, String hostName, InputStream in, OutputStream out) {
        super(serializer, hostName, in, out);
    }

    @Override
    protected ServerHttpId serverId(HttpId id) {
        return new ServerHttpId(id.sessionId, id.transactionId);
    }

    @Override
    public String newSessionId() {
        return null;
    }

    @Override
    public HttpId session(ServerHttpId id, String sessionId) {
        return new HttpId(sessionId, null);
    }

    @Override
    public HttpId transaction(ServerHttpId id, long transactionId) {
        return new HttpId(id.sessionId, transactionId);
    }
}
