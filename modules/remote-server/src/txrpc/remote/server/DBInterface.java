package txrpc.remote.server;

import txrpc.api.ISimpleTransaction;
import txrpc.api.ITransaction;
import txrpc.remote.common.IRemoteDBInterface;
import txrpc.runtime.SessionContext;
import txrpc.runtime.SimpleTransaction;
import txrpc.runtime.Transaction;
import txrpc.runtime.TxRpcGlobalContext;

import java.sql.SQLException;

final class DBInterface implements IRemoteDBInterface {

    private final SessionContext session;
    private final TxRpcGlobalContext global;
    private final TxRpcLogger logger;
    private final boolean server;
    private final long sessionOrderId;

    DBInterface(SessionContext session, TxRpcGlobalContext global, TxRpcLogger logger,
                long sessionOrderId, boolean server) {
        this.session = session;
        this.global = global;
        this.logger = logger;
        this.sessionOrderId = sessionOrderId;
        this.server = server;
        if (LocalConnectionFactory.TRACE) {
            logger.info("Opened " + getConnectionName());
        }
    }

    private String getConnectionName() {
        return server ? "connection" : "local connection";
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
    public void ping() {
    }

    void close(boolean explicit) {
        if (LocalConnectionFactory.TRACE) {
            if (explicit) {
                logger.info("Closing " + getConnectionName());
            } else {
                logger.info("Closing inactive " + getConnectionName());
            }
        }
        try {
            session.close();
        } catch (SQLException ex) {
            logger.error(ex);
        }
        global.fireSessionListeners(listener -> listener.closed(sessionOrderId));
    }

    @Override
    public void close() {
        close(true);
    }

    @Override
    public Object getUserObject() {
        return session.getUserObject();
    }
}
