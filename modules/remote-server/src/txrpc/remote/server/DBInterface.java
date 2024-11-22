package txrpc.remote.server;

import txrpc.api.IDBInterface;
import txrpc.api.ISimpleTransaction;
import txrpc.api.ITransaction;
import txrpc.runtime.SessionContext;
import txrpc.runtime.SimpleTransaction;
import txrpc.runtime.Transaction;
import txrpc.runtime.TxRpcGlobalContext;

import java.sql.SQLException;

class DBInterface implements IDBInterface {

    private final SessionContext session;
    private final TxRpcGlobalContext global;
    private final TxRpcLogger logger;
    private final long sessionOrderId;

    DBInterface(SessionContext session, TxRpcGlobalContext global, TxRpcLogger logger, long sessionOrderId) {
        this.session = session;
        this.global = global;
        this.logger = logger;
        this.sessionOrderId = sessionOrderId;
        if (LocalConnectionFactory.TRACE) {
            logger.info("Opened " + getConnectionName());
        }
    }

    protected String getConnectionName() {
        return "connection";
    }

    @Override
    public final ISimpleTransaction getSimpleTransaction() {
        return new SimpleTransaction(global, session);
    }

    @Override
    public final ITransaction getTransaction() {
        return new Transaction(global, session);
    }

    final void doClose() {
        try {
            session.close();
        } catch (SQLException ex) {
            logger.error(ex);
        }
        global.fireSessionListeners(listener -> listener.closed(sessionOrderId));
    }

    @Override
    public final void close() {
        if (LocalConnectionFactory.TRACE) {
            logger.info("Closing " + getConnectionName());
        }
        doClose();
    }
}
