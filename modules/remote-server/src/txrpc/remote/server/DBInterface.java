package txrpc.remote.server;

import txrpc.api.IDBInterface;
import txrpc.api.ISimpleTransaction;
import txrpc.api.ITransaction;
import txrpc.runtime.SessionContext;
import txrpc.runtime.SimpleTransaction;
import txrpc.runtime.Transaction;
import txrpc.runtime.TxRpcGlobalContext;

import java.sql.SQLException;

final class DBInterface implements IDBInterface {

    private final SessionContext session;
    private final TxRpcGlobalContext global;
    private final TxRpcLogger logger;
    private final long sessionOrderId;

    DBInterface(SessionContext session, TxRpcGlobalContext global, TxRpcLogger logger, long sessionOrderId) {
        this.session = session;
        this.global = global;
        this.logger = logger;
        this.sessionOrderId = sessionOrderId;
    }

    @Override
    public ISimpleTransaction getSimpleTransaction() {
        return new SimpleTransaction(global, session);
    }

    @Override
    public ITransaction getTransaction() {
        return new Transaction(global, session);
    }

    @Override
    public void close() {
        try {
            session.close();
        } catch (SQLException ex) {
            logger.error(ex);
        }
        global.fireSessionListeners(listener -> listener.closed(sessionOrderId));
    }
}
