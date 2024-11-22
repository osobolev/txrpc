package txrpc.remote.client;

import txrpc.remote.common.IConnectionFactory;
import txrpc.remote.common.IRemoteDBInterface;
import txrpc.remote.common.RemoteException;
import txrpc.remote.common.TxRpcInteraction;

import java.io.IOException;
import java.sql.SQLException;

/**
 * {@link IConnectionFactory} implementation for remote calls.
 */
public final class HttpConnectionFactory implements IConnectionFactory {

    private final TxRpcInteraction<IClientSessionId> interaction;

    public HttpConnectionFactory(TxRpcInteraction<IClientSessionId> interaction) {
        this.interaction = interaction;
    }

    @Override
    public IRemoteDBInterface openConnection(String user, String password) throws SQLException {
        try {
            IClientSessionId sessionId = interaction.open(user, password).rethrow(SQLException.class);
            return new HttpDBInterface(interaction, sessionId);
        } catch (IOException ex) {
            throw new RemoteException(ex);
        }
    }
}
