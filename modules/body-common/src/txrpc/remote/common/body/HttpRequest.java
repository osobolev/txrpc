package txrpc.remote.common.body;

import txrpc.api.IDBCommon;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.function.Consumer;

public final class HttpRequest implements Serializable {

    private static final HttpCommand[] COMMANDS = HttpCommand.values();

    public final Object id;
    public final byte commandIndex;
    public final Class<? extends IDBCommon> iface;
    public final String method;
    public final Class<?>[] paramTypes;
    public final Object[] params;
    public final int[] streamIndexes;

    public HttpRequest(Object id, HttpCommand command, Class<? extends IDBCommon> iface, String method, Class<?>[] paramTypes, Object[] params, int[] streamIndexes) {
        this.id = id;
        this.commandIndex = (byte) command.ordinal();
        this.iface = iface;
        this.method = method;
        this.paramTypes = paramTypes;
        this.params = params;
        this.streamIndexes = streamIndexes;
    }

    public HttpCommand getCommand() {
        return COMMANDS[commandIndex];
    }

    @SuppressWarnings("unchecked")
    public static Class<Object> getStreamItemType(Method method, int streamIndex) {
        Type genType = method.getGenericParameterTypes()[streamIndex];
        if (!(genType instanceof ParameterizedType))
            return null;
        ParameterizedType pType = (ParameterizedType) genType;
        if (!Consumer.class.equals(pType.getRawType()))
            return null;
        Type[] typeArgs = pType.getActualTypeArguments();
        if (typeArgs.length != 1)
            return null;
        Type arg1 = typeArgs[0];
        if (!(arg1 instanceof Class<?>))
            return null;
        return (Class<Object>) arg1;
    }
}
