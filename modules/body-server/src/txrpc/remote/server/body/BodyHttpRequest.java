package txrpc.remote.server.body;

import txrpc.remote.common.Either;
import txrpc.remote.common.RemoteException;
import txrpc.remote.common.TxRpcInteraction;
import txrpc.remote.common.body.*;
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

    private static Method findMethod(HttpRequest data) {
        try {
            return data.iface.getMethod(data.method, data.paramTypes);
        } catch (Throwable ex) {
            throw new RemoteException("Method not found");
        }
    }

    private Either<?> getResult(TxRpcInteraction<IServerSessionId> interaction) throws IOException {
        HttpRequest data = request.requestData();
        HttpId id = data.id;
        IServerSessionId sessionId = request.sessionId(id);
        HttpCommand command = data.getCommand();
        switch (command) {
        case OPEN: {
            String user = (String) data.params[0];
            String password = (String) data.params[1];
            return interaction
                .open(user, password)
                .map(session -> new HttpDBInterfaceInfo(request.sessionWireId(session.sessionId), session.userObject));
        }
        case GET_TRANSACTION: {
            return interaction.beginTransaction(sessionId);
        }
        case COMMIT:
        case ROLLBACK: {
            return interaction.endTransaction(sessionId, request.transactionId(id), command == HttpCommand.COMMIT);
        }
        case INVOKE: {
            Method method = findMethod(data);
            return interaction.invoke(sessionId, request.transactionId(id), method, data.params);
        }
        case PING:
            return interaction.ping(sessionId);
        case CLOSE:
            return interaction.close(sessionId);
        }
        throw new RemoteException("Unknown command");
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
