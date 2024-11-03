package txrpc.remote.server;

import txrpc.remote.common.IConnectionFactory;
import txrpc.remote.common.IRemoteDBInterface;
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

        T create(SessionContext session, long sessionOrderId);
    }

    <T> T openConnection(String user, String password, String host,
                         DBInterfaceFactory<T> factory) throws SQLException {
        long sessionOrderId = connectionCount.getAndIncrement();
        SessionContext session = sessionFactory.login(logger, sessionOrderId, user, password);
        T result = factory.create(session, sessionOrderId);
        global.fireSessionListeners(listener -> listener.opened(sessionOrderId, user, host, session.getUserObject()));
        return result;
    }

    @Override
    public IRemoteDBInterface openConnection(String user, String password) throws SQLException {
        return openConnection(
            user, password, null,
            (session, sessionOrderId) -> new LocalDBInterface(session, global, logger, sessionOrderId)
        );
    }
}
