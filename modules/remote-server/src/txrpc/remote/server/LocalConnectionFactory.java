package txrpc.remote.server;

import txrpc.remote.common.IConnectionFactory;
import txrpc.remote.common.IRemoteDBInterface;
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

    private final boolean server;
    private final SessionFactory sessionFactory;
    final TxRpcLogger logger;
    final TxRpcGlobalContext global;

    private final AtomicLong connectionCount = new AtomicLong(0);

    /**
     * Constructor.
     */
    public LocalConnectionFactory(SessionFactory sessionFactory, TxRpcLogger logger, TxRpcGlobalContext global) {
        this(sessionFactory, logger, global, false);
    }

    LocalConnectionFactory(SessionFactory sessionFactory, TxRpcLogger logger, TxRpcGlobalContext global, boolean server) {
        this.sessionFactory = sessionFactory;
        this.logger = logger;
        this.global = global;
        this.server = server;
    }

    <T> T openConnection(String user, String password, String host,
                         Function<DBInterface, T> onCreate) throws SQLException {
        long sessionOrderId = connectionCount.getAndIncrement();
        SessionContext session = sessionFactory.login(logger, sessionOrderId, user, password);
        DBInterface db = new DBInterface(session, global, logger, sessionOrderId, server);
        T result = onCreate.apply(db);
        global.fireSessionListeners(listener -> listener.opened(sessionOrderId, user, host, session.getUserObject()));
        return result;
    }

    public IRemoteDBInterface openConnection(String user, String password) throws SQLException {
        return openConnection(user, password, null, Function.identity());
    }
}
