package txrpc.remote.server.body;

import txrpc.remote.server.IServerSessionId;

public final class SimpleServerSessionId implements IServerSessionId {

    private final String id;

    private SimpleServerSessionId(String id) {
        this.id = id;
    }

    public static IServerSessionId create(String id) {
        return id == null ? null : new SimpleServerSessionId(id);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return id;
    }
}
