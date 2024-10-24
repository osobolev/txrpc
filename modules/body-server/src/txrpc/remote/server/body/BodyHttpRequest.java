package txrpc.remote.server.body;

import txrpc.remote.common.Either;
import txrpc.remote.common.TxRpcInteraction;
import txrpc.remote.common.body.HttpCommand;
import txrpc.remote.common.body.HttpDBInterfaceInfo;
import txrpc.remote.common.body.HttpResult;
import txrpc.remote.server.IHttpRequest;
import txrpc.remote.server.IServerSessionId;

import java.io.IOException;
import java.lang.reflect.Method;

public final class BodyHttpRequest implements IHttpRequest {

    private final String hostName;
    private final IBodyInteraction request;

    public BodyHttpRequest(String hostName, IBodyInteraction request) {
        this.hostName = hostName;
        this.request = request;
    }

    @Override
    public String hostName() {
        return hostName;
    }

    @Override
    public IServerSessionId newSessionId() {
        return request.newSessionId();
    }

    private static Either<Method> findMethod(ServerHttpRequest data) {
        try {
            Method method = data.iface.getMethod(data.method, data.paramTypes);
            return Either.ok(method);
        } catch (Throwable ex) {
            return Either.error("Method not found");
        }
    }

    private static IServerSessionId sessionId(ServerHttpId id) {
        return id.sessionId;
    }

    private static String transactionId(ServerHttpId id) {
        return id.transactionId == null ? null : id.transactionId.toString();
    }

    private Either<?> getResult(TxRpcInteraction<IServerSessionId> interaction) throws IOException {
        ServerHttpRequest data = request.requestData();
        ServerHttpId id = data.id;
        switch (data.command) {
        case OPEN: {
            String user = (String) data.params[0];
            String password = (String) data.params[1];
            return interaction
                .open(user, password)
                .map(session -> new HttpDBInterfaceInfo(request.session(id, session.sessionId), session.userObject));
        }
        case GET_TRANSACTION: {
            return interaction
                .beginTransaction(sessionId(id))
                .map(transactionId -> request.transaction(id, Long.parseLong(transactionId)));
        }
        case COMMIT:
        case ROLLBACK: {
            String transactionId = transactionId(id);
            return interaction.endTransaction(sessionId(id), transactionId, data.command == HttpCommand.COMMIT);
        }
        case INVOKE: {
            String transactionId = transactionId(id);
            return findMethod(data)
                .then(method -> interaction.invoke(sessionId(id), transactionId, method, data.params));
        }
        case PING:
            return interaction.ping(sessionId(id));
        case CLOSE:
            return interaction.close(sessionId(id));
        }
        return Either.error("Unknown command");
    }

    @Override
    public void perform(TxRpcInteraction<IServerSessionId> interaction) throws IOException {
        Either<?> result = getResult(interaction);
        request.write(new HttpResult(result.getResult(), result.getError()));
    }

    @Override
    public void writeError(Throwable error) throws IOException {
        request.write(new HttpResult(null, error));
    }
}
