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

    protected abstract IClientSessionId newSessionId();

    protected abstract IClientSessionId newSessionId(IClientSessionId sessionId, HttpId id);

    protected abstract HttpId wireId(IClientSessionId sessionId, String transactionId);

    private static Either<Object> serverException(Throwable error) {
        StackTraceElement[] serverST = error.getStackTrace();
        StackTraceElement[] clientST = new Throwable().getStackTrace();
        StackTraceElement[] allST = new StackTraceElement[serverST.length + clientST.length];
        System.arraycopy(serverST, 0, allST, 0, serverST.length);
        System.arraycopy(clientST, 0, allST, serverST.length, clientST.length);
        error.setStackTrace(allST);
        return Either.error(error);
    }

    private Either<Object> httpInvoke(Class<?> retType, IClientSessionId sessionId, String transactionId,
                                      HttpCommand command, Class<? extends IDBCommon> iface, String method,
                                      Class<?>[] paramTypes, Object[] params) throws IOException {
        HttpId id = wireId(sessionId, transactionId);
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
    private <T> Either<T> httpInvoke(Class<T> retType, IClientSessionId sessionId, String transactionId,
                                     HttpCommand command, Object... params) throws IOException {
        return (Either<T>) httpInvoke(retType, sessionId, transactionId, command, null, null, null, params);
    }

    @Override
    public final Either<NewSession<IClientSessionId>> open(String user, String password) throws IOException {
        IClientSessionId sessionId = newSessionId();
        return httpInvoke(HttpDBInterfaceInfo.class, sessionId, null, HttpCommand.OPEN, user, password)
            .map(info -> new NewSession<>(newSessionId(sessionId, info.id), info.userObject));
    }

    @Override
    public final Either<String> beginTransaction(IClientSessionId sessionId) throws IOException {
        return httpInvoke(HttpId.class, sessionId, null, HttpCommand.GET_TRANSACTION)
            .map(tid -> tid.transactionId.toString());
    }

    @Override
    public final Either<Void> endTransaction(IClientSessionId sessionId, String transactionId, boolean commit) throws IOException {
        return httpInvoke(void.class, sessionId, transactionId, commit ? HttpCommand.COMMIT : HttpCommand.ROLLBACK);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final Either<Object> invoke(IClientSessionId sessionId, String transactionId, Method method, Object[] args) throws IOException {
        Class<? extends IDBCommon> iface = (Class<? extends IDBCommon>) method.getDeclaringClass();
        return httpInvoke(
            method.getReturnType(), sessionId, transactionId, HttpCommand.INVOKE,
            iface, method.getName(), method.getParameterTypes(), args
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
