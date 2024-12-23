package txrpc.remote.server;

import txrpc.api.IDBInterface;
import txrpc.api.ISimpleTransaction;
import txrpc.api.ITransaction;
import txrpc.remote.common.IConnectionFactory;
import txrpc.runtime.SessionContext;
import txrpc.runtime.TxRpcGlobalContext;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

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

    <T> T openConnection(String user, String password, String host,
                         Function<IDBInterface, T> mapResult) throws SQLException {
        long sessionOrderId = connectionCount.getAndIncrement();
        SessionContext session = sessionFactory.login(logger, sessionOrderId, user, password);
        DBInterface db = new DBInterface(global, session, logger, sessionOrderId);
        if (TRACE) {
            logger.info("Opened connection");
        }
        T result = mapResult.apply(db);
        global.fireSessionListeners(listener -> listener.opened(sessionOrderId, user, host, session.getUserObject()));
        return result;
    }

    @Override
    public IDBInterface openConnection(String user, String password) throws SQLException {
        IDBInterface db = openConnection(user, password, null, Function.identity());
        return new IDBInterface() {

            @Override
            public ISimpleTransaction getSimpleTransaction() throws SQLException {
                return db.getSimpleTransaction();
            }

            @Override
            public ITransaction getTransaction() throws SQLException {
                return db.getTransaction();
            }

            @Override
            public void close() throws SQLException {
                if (TRACE) {
                    logger.info("Closing connection");
                }
                db.close();
            }
        };
    }
}
