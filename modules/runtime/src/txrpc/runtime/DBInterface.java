package txrpc.runtime;

import txrpc.api.IDBInterface;
import txrpc.api.ISimpleTransaction;
import txrpc.api.ITransaction;

import java.sql.SQLException;

final class DBInterface implements IDBInterface {

    private final TxRpcGlobalContext global;
    private final SessionContext session;
    private final TxRpcLogger logger;
    private final long sessionOrderId;

    DBInterface(TxRpcGlobalContext global, SessionContext session, TxRpcLogger logger, long sessionOrderId) {
        this.global = global;
        this.session = session;
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
