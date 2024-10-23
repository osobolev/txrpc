package txrpc.remote.client;

import txrpc.api.ITransaction;
import txrpc.remote.common.RemoteException;
import txrpc.remote.common.TxRpcInteraction;

import java.io.IOException;
import java.sql.SQLException;

final class HttpTransaction extends HttpSimpleTransaction implements ITransaction {

    private final String transactionId;

    HttpTransaction(TxRpcInteraction<IClientSessionId> interaction, IClientSessionId sessionId, String transactionId) {
        super(interaction, sessionId);
        this.transactionId = transactionId;
    }

    @Override
    protected String getTransactionId() {
        return transactionId;
    }

    @Override
    public void rollback() throws SQLException {
        try {
            interaction.endTransaction(sessionId, transactionId, true).rethrow(SQLException.class);
        } catch (IOException ex) {
            throw new RemoteException(ex);
        }
    }

    @Override
    public void commit() throws SQLException {
        try {
            interaction.endTransaction(sessionId, transactionId, false).rethrow(SQLException.class);
        } catch (IOException ex) {
            throw new RemoteException(ex);
        }
    }
}
