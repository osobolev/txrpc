package txrpc.remote.common.body;

import txrpc.api.IDBCommon;

import java.io.Serializable;

public final class HttpRequest implements Serializable {

    private static final HttpCommand[] COMMANDS = HttpCommand.values();

    public final Object id;
    public final byte commandIndex;
    public final Class<? extends IDBCommon> iface;
    public final String method;
    public final Class<?>[] paramTypes;
    public final Object[] params;

    public HttpRequest(Object id, HttpCommand command, Class<? extends IDBCommon> iface, String method, Class<?>[] paramTypes, Object[] params) {
        this.id = id;
        this.commandIndex = (byte) command.ordinal();
        this.iface = iface;
        this.method = method;
        this.paramTypes = paramTypes;
        this.params = params;
    }

    public HttpCommand getCommand() {
        return COMMANDS[commandIndex];
    }
}
