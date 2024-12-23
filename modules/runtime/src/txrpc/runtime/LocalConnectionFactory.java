package txrpc.runtime;

import txrpc.api.IConnectionFactory;
import txrpc.api.IDBInterface;
import txrpc.api.ISimpleTransaction;
import txrpc.api.ITransaction;

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
    private final TxRpcLogger logger;
    private final TxRpcGlobalContext global;

    private final AtomicLong connectionCount = new AtomicLong(0);

    public LocalConnectionFactory(SessionFactory sessionFactory, TxRpcLogger logger, TxRpcGlobalContext global) {
        this.sessionFactory = sessionFactory;
        this.logger = logger;
        this.global = global;
    }

    public <T> T openConnection(String user, String password, String host,
                                Function<IDBInterface, T> mapResult) throws SQLException {
        long sessionOrderId = connectionCount.getAndIncrement();
        SessionContext session = sessionFactory.login(logger, sessionOrderId, user, password);
        DBInterface db = new DBInterface(global, session, logger, sessionOrderId);
        T result = mapResult.apply(db);
        global.fireSessionListeners(listener -> listener.opened(sessionOrderId, user, host, session.getUserObject()));
        return result;
    }

    @Override
    public IDBInterface openConnection(String user, String password) throws SQLException {
        return openConnection(user, password, null, db -> {
            if (TRACE) {
                logger.info("Opened local connection");
            }
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
                        logger.info("Closing local connection");
                    }
                    db.close();
                }
            };
        });
    }
}
