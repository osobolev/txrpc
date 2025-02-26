package txrpc.remote.client.body;

import txrpc.api.IDBCommon;
import txrpc.remote.client.IClientSessionId;
import txrpc.remote.common.Either;
import txrpc.remote.common.TxRpcInteraction;
import txrpc.remote.common.body.HttpCommand;
import txrpc.remote.common.body.HttpRequest;
import txrpc.remote.common.body.HttpResult;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;

public abstract class BaseClientBodyInteraction implements TxRpcInteraction<IClientSessionId> {

    private final IHttpClient client;

    protected BaseClientBodyInteraction(IHttpClient client) {
        this.client = client;
    }

    protected abstract IClientSessionId newSessionId();

    protected abstract IClientSessionId newSessionId(IClientSessionId sessionId, Object newSessionWireId);

    protected abstract Object wireId(IClientSessionId sessionId, String transactionId);

    private static Either<Object> serverException(Throwable error) {
        StackTraceElement[] serverST = error.getStackTrace();
        StackTraceElement[] clientST = new Throwable().getStackTrace();
        StackTraceElement[] allST = new StackTraceElement[serverST.length + clientST.length];
        System.arraycopy(serverST, 0, allST, 0, serverST.length);
        System.arraycopy(clientST, 0, allST, serverST.length, clientST.length);
        error.setStackTrace(allST);
        return Either.error(error);
    }

    private Either<Object> doInvoke(Class<?> retType, IClientSessionId sessionId, String transactionId,
                                    HttpCommand command, Class<? extends IDBCommon> iface,
                                    Method method, Object[] params, int[] streamIndexes) throws IOException {
        String methodName;
        Class<?>[] paramTypes;
        if (method == null) {
            methodName = null;
            paramTypes = null;
        } else {
            methodName = method.getName();
            paramTypes = method.getParameterTypes();
        }
        Object id = wireId(sessionId, transactionId);
        HttpRequest request = new HttpRequest(id, command, iface, methodName, paramTypes, params, streamIndexes);
        HttpResult result = client.call(retType, sessionId, method, request);
        Throwable error = result.error;
        if (error != null) {
            return serverException(error);
        } else {
            return Either.ok(result.result);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> Either<T> httpInvoke(Class<T> retType, IClientSessionId sessionId, String transactionId,
                                     HttpCommand command, Object... params) throws IOException {
        return (Either<T>) doInvoke(
            retType, sessionId, transactionId, command, null, null, params, null
        );
    }

    @Override
    public final Either<IClientSessionId> open(String user, String password) throws IOException {
        IClientSessionId sessionId = newSessionId();
        return httpInvoke(Object.class, sessionId, null, HttpCommand.OPEN, user, password)
            .map(wireId -> newSessionId(sessionId, wireId));
    }

    @Override
    public final Either<String> beginTransaction(IClientSessionId sessionId) throws IOException {
        return httpInvoke(String.class, sessionId, null, HttpCommand.GET_TRANSACTION);
    }

    @Override
    public final Either<Void> endTransaction(IClientSessionId sessionId, String transactionId, boolean commit) throws IOException {
        return httpInvoke(void.class, sessionId, transactionId, commit ? HttpCommand.COMMIT : HttpCommand.ROLLBACK);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final Either<Object> invoke(IClientSessionId sessionId, String transactionId, Method method, Object[] args) throws IOException {
        Class<? extends IDBCommon> iface = (Class<? extends IDBCommon>) method.getDeclaringClass();
        int[] streamIndexes = null;
        int nstreams = 0;
        Class<?>[] paramTypes = method.getParameterTypes();
        for (int i = 0; i < paramTypes.length; i++) {
            Class<Object> itemType = HttpRequest.getStreamItemType(method, i);
            if (itemType != null) {
                if (streamIndexes == null) {
                    streamIndexes = new int[paramTypes.length];
                }
                streamIndexes[nstreams++] = i;
            }
        }
        if (streamIndexes != null) {
            streamIndexes = Arrays.copyOf(streamIndexes, nstreams);
        }
        return doInvoke(
            method.getReturnType(), sessionId, transactionId, HttpCommand.INVOKE,
            iface, method, args, streamIndexes
        );
    }

    @Override
    public final Either<Void> ping(IClientSessionId sessionId) throws IOException {
        return httpInvoke(void.class, sessionId, null, HttpCommand.PING);
    }

    @Override
    public final Either<Void> close(IClientSessionId sessionId) throws IOException {
        return httpInvoke(void.class, sessionId, null, HttpCommand.CLOSE);
    }
}
