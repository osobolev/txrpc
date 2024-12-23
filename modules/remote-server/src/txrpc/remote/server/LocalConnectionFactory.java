package txrpc.remote.server;

import txrpc.api.IDBInterface;
import txrpc.api.ISimpleTransaction;
import txrpc.api.ITransaction;
import txrpc.remote.common.IConnectionFactory;
import txrpc.runtime.SessionContext;
import txrpc.runtime.TxRpcGlobalContext;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * {@link IConnectionFactory} implementation for local connection.
 */
public final class LocalConnectionFactory implements IConnectionFactory {

    /**
     * true if output connection open/close information
     */
    public static boolean TRACE = true;

    private final SessionFactory sessionFactory;
    final TxRpcLogger logger;
    final TxRpcGlobalContext global;

    private final AtomicLong connectionCount = new AtomicLong(0);

    public LocalConnectionFactory(SessionFactory sessionFactory, TxRpcLogger logger, TxRpcGlobalContext global) {
        this.sessionFactory = sessionFactory;
        this.logger = logger;
        this.global = global;
    }

    interface DBInterfaceFactory<T> {

        T create(DBInterface db);
    }

    <T> T openConnection(String user, String password, String host,
                         DBInterfaceFactory<T> factory) throws SQLException {
        long sessionOrderId = connectionCount.getAndIncrement();
        SessionContext session = sessionFactory.login(logger, sessionOrderId, user, password);
        DBInterface db = new DBInterface(session, global, logger, sessionOrderId);
        if (TRACE) {
            logger.info("Opened connection");
        }
        T result = factory.create(db);
        global.fireSessionListeners(listener -> listener.opened(sessionOrderId, user, host, session.getUserObject()));
        return result;
    }

    @Override
    public IDBInterface openConnection(String user, String password) throws SQLException {
        return openConnection(
            user, password, null,
            db -> new IDBInterface() {

                @Override
                public ISimpleTransaction getSimpleTransaction() {
                    return db.getSimpleTransaction();
                }

                @Override
                public ITransaction getTransaction() {
                    return db.getTransaction();
                }

                @Override
                public void close() {
                    if (TRACE) {
                        logger.info("Closing connection");
                    }
                    db.close();
                }
            }
        );
    }
}
