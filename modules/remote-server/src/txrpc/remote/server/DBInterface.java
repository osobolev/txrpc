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
    private final boolean local;

    DBInterface(SessionContext session, TxRpcGlobalContext global, TxRpcLogger logger, long sessionOrderId, boolean local) {
        this.session = session;
        this.global = global;
        this.logger = logger;
        this.sessionOrderId = sessionOrderId;
        this.local = local;
        if (LocalConnectionFactory.TRACE) {
            logger.info("Opened " + getConnectionName());
        }
    }

    String getConnectionName() {
        return local ? "local connection" : "connection";
    }

    @Override
    public ISimpleTransaction getSimpleTransaction() {
        return new SimpleTransaction(global, session);
    }

    @Override
    public ITransaction getTransaction() {
        return new Transaction(global, session);
    }

    void doClose() {
        try {
            session.close();
        } catch (SQLException ex) {
            logger.error(ex);
        }
        global.fireSessionListeners(listener -> listener.closed(sessionOrderId));
    }

    @Override
    public void close() {
        if (LocalConnectionFactory.TRACE) {
            logger.info("Closing " + getConnectionName());
        }
        doClose();
    }
}
