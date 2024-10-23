package txrpc.remote.client.body;

import txrpc.api.IDBCommon;
import txrpc.remote.client.IClientSessionId;
import txrpc.remote.common.Either;
import txrpc.remote.common.TxRpcInteraction;
import txrpc.remote.common.body.*;

import java.io.IOException;
import java.lang.reflect.Method;

public abstract class BaseClientBodyInteraction implements TxRpcInteraction<IClientSessionId> {

    private final IHttpClient client;

    protected BaseClientBodyInteraction(IHttpClient client) {
        this.client = client;
    }

    private Either<Object> httpInvoke(Class<?> retType, IClientSessionId sessionId,
                                      HttpCommand command, HttpId id, Class<? extends IDBCommon> iface, String method,
                                      Class<?>[] paramTypes, Object[] params) throws IOException {
        HttpRequest request = new HttpRequest(id, command, iface, method, paramTypes, params);
        HttpResult result = client.call(retType, sessionId, request);
        Throwable error = result.error;
        if (error != null) {
            return serverException(error);
        } else {
            return Either.ok(result.result);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> Either<T> httpInvoke(Class<T> retType, IClientSessionId sessionId, HttpCommand command, HttpId id, Object... params) throws IOException {
        return (Either<T>) httpInvoke(retType, sessionId, command, id, null, null, null, params);
    }

    private static Either<Object> serverException(Throwable error) {
        StackTraceElement[] serverST = error.getStackTrace();
        StackTraceElement[] clientST = new Throwable().getStackTrace();
        StackTraceElement[] allST = new StackTraceElement[serverST.length + clientST.length];
        System.arraycopy(serverST, 0, allST, 0, serverST.length);
        System.arraycopy(clientST, 0, allST, serverST.length, clientST.length);
        error.setStackTrace(allST);
        return Either.error(error);
    }

    protected abstract IClientSessionId newSessionId();

    protected abstract IClientSessionId newSessionId(IClientSessionId sessionId, HttpDBInterfaceInfo info);

    protected abstract HttpId id(IClientSessionId sessionId, String transactionId);

    @Override
    public final Either<NewSession<IClientSessionId>> open(String user, String password) throws IOException {
        HttpId id = new HttpId();
        IClientSessionId sessionId = newSessionId();
        return httpInvoke(HttpDBInterfaceInfo.class, sessionId, HttpCommand.OPEN, id, user, password)
            .map(info -> new NewSession<>(newSessionId(sessionId, info), info.userObject));
    }

    @Override
    public final Either<String> beginTransaction(IClientSessionId sessionId) throws IOException {
        HttpId id = id(sessionId, null);
        return httpInvoke(HttpId.class, sessionId, HttpCommand.GET_TRANSACTION, id)
            .map(tid -> tid.transactionId.toString());
    }

    @Override
    public final Either<Void> endTransaction(IClientSessionId sessionId, String transactionId, boolean commit) throws IOException {
        HttpId id = id(sessionId, transactionId);
        return httpInvoke(void.class, sessionId, commit ? HttpCommand.COMMIT : HttpCommand.ROLLBACK, id);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final Either<Object> invoke(IClientSessionId sessionId, String transactionId, Method method, Object[] args) throws IOException {
        HttpId id = id(sessionId, transactionId);
        Class<? extends IDBCommon> iface = (Class<? extends IDBCommon>) method.getDeclaringClass();
        return httpInvoke(method.getReturnType(), sessionId, HttpCommand.INVOKE, id, iface, method.getName(), method.getParameterTypes(), args);
    }

    @Override
    public final Either<Void> ping(IClientSessionId sessionId) throws IOException {
        HttpId id = id(sessionId, null);
        return httpInvoke(void.class, sessionId, HttpCommand.PING, id);
    }

    @Override
    public final Either<Void> close(IClientSessionId sessionId) throws IOException {
        HttpId id = id(sessionId, null);
        return httpInvoke(void.class, sessionId, HttpCommand.CLOSE, id);
    }
}
