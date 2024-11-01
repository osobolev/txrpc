package txrpc.remote.server.body;

import txrpc.remote.common.Either;
import txrpc.remote.common.RemoteException;
import txrpc.remote.common.TxRpcInteraction;
import txrpc.remote.common.body.*;
import txrpc.remote.server.IHttpRequest;
import txrpc.remote.server.IServerSessionId;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;

public abstract class BaseBodyHttpRequest implements IHttpRequest {

    private final ISerializer serializer;
    private final String hostName;
    private final InputStream in;
    private final OutputStream out;

    protected BaseBodyHttpRequest(ISerializer serializer, String hostName, InputStream in, OutputStream out) {
        this.serializer = serializer;
        this.hostName = hostName;
        this.in = in;
        this.out = out;
    }

    @Override
    public final String hostName() {
        return hostName;
    }

    protected abstract Object newSessionWireId(IServerSessionId sessionId);

    protected abstract IServerSessionId sessionId(Object wireId);

    protected abstract String transactionId(Object wireId);

    private static Method findMethod(HttpRequest data) {
        try {
            return data.iface.getMethod(data.method, data.paramTypes);
        } catch (Throwable ex) {
            throw new RemoteException("Method not found");
        }
    }

    private Either<?> getResult(TxRpcInteraction<IServerSessionId> interaction) throws IOException {
        HttpRequest data;
        try (ISerializer.Reader fromClient = serializer.newReader(in)) {
            data = fromClient.read(HttpRequest.class);
        }
        Object wireId = data.id;
        IServerSessionId sessionId = sessionId(wireId);
        HttpCommand command = data.getCommand();
        switch (command) {
        case OPEN:
            String user = (String) data.params[0];
            String password = (String) data.params[1];
            return interaction
                .open(user, password)
                .map(session -> new HttpDBInterfaceInfo(newSessionWireId(session.sessionId), session.userObject));
        case GET_TRANSACTION:
            return interaction.beginTransaction(sessionId);
        case COMMIT:
        case ROLLBACK:
            return interaction.endTransaction(sessionId, transactionId(wireId), command == HttpCommand.COMMIT);
        case INVOKE:
            Method method = findMethod(data);
            return interaction.invoke(sessionId, transactionId(wireId), method, data.params);
        case PING:
            return interaction.ping(sessionId);
        case CLOSE:
            return interaction.close(sessionId);
        }
        throw new RemoteException("Unknown command");
    }

    private void write(HttpResult result) throws IOException {
        try (ISerializer.Writer toClient = serializer.newWriter(out)) {
            toClient.write(result, HttpResult.class);
        }
    }

    @Override
    public final void perform(TxRpcInteraction<IServerSessionId> interaction) throws IOException {
        Either<?> result = getResult(interaction);
        write(new HttpResult(result.getResult(), result.getError()));
    }

    @Override
    public final void writeError(Throwable error) throws IOException {
        write(new HttpResult(null, error));
    }
}
