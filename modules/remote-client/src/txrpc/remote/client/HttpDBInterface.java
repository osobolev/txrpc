package txrpc.remote.client;

import txrpc.api.ISimpleTransaction;
import txrpc.api.ITransaction;
import txrpc.remote.common.IRemoteDBInterface;
import txrpc.remote.common.RemoteException;
import txrpc.remote.common.TxRpcInteraction;

import java.io.IOException;
import java.sql.SQLException;

final class HttpDBInterface implements IRemoteDBInterface {

    private final TxRpcInteraction<IClientSessionId> interaction;
    private final IClientSessionId sessionId;
    private final Object userObject;

    HttpDBInterface(TxRpcInteraction<IClientSessionId> interaction, IClientSessionId sessionId, Object userObject) {
        this.interaction = interaction;
        this.sessionId = sessionId;
        this.userObject = userObject;
    }

    @Override
    public ISimpleTransaction getSimpleTransaction() {
        return new HttpSimpleTransaction(interaction, sessionId);
    }

    @Override
    public ITransaction getTransaction() throws SQLException {
        try {
            String transactionId = interaction.beginTransaction(sessionId).rethrow(SQLException.class);
            return new HttpTransaction(interaction, sessionId, transactionId);
        } catch (IOException ex) {
            throw new RemoteException(ex);
        }
    }

    @Override
    public void ping() {
        try {
            interaction.ping(sessionId).rethrow(RuntimeException.class);
        } catch (IOException ex) {
            throw new RemoteException(ex);
        }
    }

    @Override
    public void close() throws SQLException {
        try {
            interaction.close(sessionId).rethrow(SQLException.class);
        } catch (IOException ex) {
            throw new RemoteException(ex);
        }
    }

    @Override
    public Object getUserObject() {
        return userObject;
    }
}
