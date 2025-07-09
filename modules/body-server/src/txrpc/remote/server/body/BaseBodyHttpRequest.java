package txrpc.remote.server.body;

import txrpc.remote.common.Either;
import txrpc.remote.common.RemoteException;
import txrpc.remote.common.TxRpcInteraction;
import txrpc.remote.common.body.HttpCommand;
import txrpc.remote.common.body.HttpRequest;
import txrpc.remote.common.body.HttpResult;
import txrpc.remote.common.body.ISerializer;
import txrpc.remote.server.IHttpRequest;
import txrpc.remote.server.IServerSessionId;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.function.Consumer;

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

    private Either<?> getResult(TxRpcInteraction<IServerSessionId> interaction,
                                HttpRequest data, ISerializer.Writer toClient) throws IOException {
        Object wireId = data.id;
        IServerSessionId sessionId = sessionId(wireId);
        HttpCommand command = data.getCommand();
        switch (command) {
        case OPEN:
            String user = (String) data.params[0];
            String password = (String) data.params[1];
            return interaction.open(user, password).map(this::newSessionWireId);
        case GET_TRANSACTION:
            return interaction.beginTransaction(sessionId);
        case COMMIT:
        case ROLLBACK:
            return interaction.endTransaction(sessionId, transactionId(wireId), command == HttpCommand.COMMIT);
        case INVOKE:
            Method method = findMethod(data);
            if (data.streamIndexes != null) {
                for (int streamIndex : data.streamIndexes) {
                    Class<Object> itemType = HttpRequest.getStreamItemType(method, streamIndex);
                    if (itemType == null) {
                        throw new RemoteException("Parameter " + streamIndex + " must be Consumer");
                    }
                    data.params[streamIndex] = (Consumer<Object>) item -> {
                        try {
                            toClient.writeStreamIndex(streamIndex);
                            toClient.write(item, itemType);
                        } catch (IOException ex) {
                            throw new RemoteException(ex);
                        }
                    };
                }
            }
            Either<Object> result = interaction.invoke(sessionId, transactionId(wireId), method, data.params);
            if (data.streamIndexes != null) {
                toClient.writeStreamIndex(-1);
            }
            return result;
        case PING:
            return interaction.ping(sessionId);
        case CLOSE:
            return interaction.close(sessionId);
        }
        throw new RemoteException("Unknown command");
    }

    @Override
    public final void perform(TxRpcInteraction<IServerSessionId> interaction) throws IOException {
        HttpRequest data;
        try (ISerializer.Reader fromClient = serializer.newReader(in)) {
            data = fromClient.read(HttpRequest.class);
        } catch (RemoteException ex) {
            try (ISerializer.Writer toClient = serializer.newWriter(out)) {
                HttpResult httpResult = new HttpResult(null, ex);
                toClient.write(httpResult, HttpResult.class);
            }
            return;
        }
        try (ISerializer.Writer toClient = serializer.newWriter(out)) {
            HttpResult httpResult;
            try {
                Either<?> result = getResult(interaction, data, toClient);
                httpResult = new HttpResult(result.getResult(), result.getError());
            } catch (RemoteException ex) {
                httpResult = new HttpResult(null, ex);
            }
            toClient.write(httpResult, HttpResult.class);
        }
    }
}
