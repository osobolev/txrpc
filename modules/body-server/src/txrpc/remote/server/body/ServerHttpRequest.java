package txrpc.remote.server.body;

import txrpc.api.IDBCommon;
import txrpc.remote.common.body.HttpCommand;

public final class ServerHttpRequest {

    public final ServerHttpId id;
    public final HttpCommand command;
    public final Class<? extends IDBCommon> iface;
    public final String method;
    public final Class<?>[] paramTypes;
    public final Object[] params;

    public ServerHttpRequest(ServerHttpId id, HttpCommand command, Class<? extends IDBCommon> iface, String method, Class<?>[] paramTypes, Object[] params) {
        this.id = id;
        this.command = command;
        this.iface = iface;
        this.method = method;
        this.paramTypes = paramTypes;
        this.params = params;
    }
}
