package txrpc.remote.server;

import txrpc.api.IDBCommon;
import txrpc.api.ISimpleTransaction;
import txrpc.api.ITransaction;
import txrpc.remote.common.Either;
import txrpc.remote.common.RemoteException;
import txrpc.remote.common.TxRpcInteraction;
import txrpc.remote.common.WatcherThread;
import txrpc.runtime.TxRpcGlobalContext;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Server-side object for HTTP access to business interfaces.
 * Method {@link #dispatch(IHttpRequest)} should be invoked from servlets.
 * <p>
 * Servlet container can have more than one {@link HttpDispatcher} object, and distinguish them by
 * application name (which should be reflected in servlet URL; for example, servlet on
 * /app1/remoting path invokes HttpDispatcher of application app1, etc).
 * <p>
 * {@link HttpDispatcher} object should be created only once for application.
 */
public final class HttpDispatcher {

    private final LocalConnectionFactory lw;
    private final WatcherThread watcher;

    private static final class DBWrapper {

        final IServerSessionId sessionId;
        final DBInterface db;
        final ConcurrentMap<String, ITransaction> transactions = new ConcurrentHashMap<>();

        DBWrapper(IServerSessionId sessionId, DBInterface db) {
            this.sessionId = sessionId;
            this.db = db;
        }

        boolean isTimedOut(long time) {
            boolean timeout = time - db.getLastActive() >= WatcherThread.ACTIVITY_CHECK_INTERVAL;
            if (timeout) {
                db.close(false);
            }
            return timeout;
        }
    }

    private final Map<String, DBWrapper> connectionMap = new HashMap<>();
    private final AtomicLong transactionCount = new AtomicLong(0);

    public HttpDispatcher(SessionFactory sessionFactory, TxRpcLogger logger, TxRpcGlobalContext global) {
        this.lw = new LocalConnectionFactory(sessionFactory, logger, global, true);
        this.watcher = new WatcherThread(1, this::checkActivity);
        this.watcher.runThread();
    }

    private void putConnection(IServerSessionId sessionId, DBWrapper db) {
        synchronized (connectionMap) {
            connectionMap.put(sessionId.getId(), db);
        }
    }

    private void endSession(IServerSessionId sessionId) {
        synchronized (connectionMap) {
            connectionMap.remove(sessionId.getId());
        }
    }

    private DBWrapper maybeSession(IServerSessionId sessionId) {
        if (sessionId == null)
            return null;
        synchronized (connectionMap) {
            return connectionMap.get(sessionId.getId());
        }
    }

    private void checkActivity() {
        long time = DBInterface.getCurrentTime();
        synchronized (connectionMap) {
            connectionMap.values().removeIf(db -> db.isTimedOut(time));
        }
    }

    private void log(Throwable ex) {
        lw.logger.error(ex);
    }

    private TxRpcInteraction<IServerSessionId> getInteraction(IHttpRequest request) {
        return new TxRpcInteraction<IServerSessionId>() {

            private Either<DBWrapper> getSession(IServerSessionId sessionId) {
                if (sessionId == null) {
                    return Either.error("No session ID");
                }
                DBWrapper db = maybeSession(sessionId);
                if (db == null) {
                    return Either.error("Session closed");
                }
                return Either.ok(db);
            }

            @Override
            public Either<NewSession<IServerSessionId>> open(String user, String password) {
                try {
                    DBWrapper dbw = lw.openConnection(
                        user, password, request.hostName(),
                        db -> {
                            IServerSessionId id = request.newSessionId();
                            DBWrapper wrapper = new DBWrapper(id, db);
                            putConnection(id, wrapper);
                            return wrapper;
                        }
                    );
                    return Either.ok(new NewSession<>(dbw.sessionId, dbw.db.getUserObject()));
                } catch (SQLException ex) {
                    log(ex);
                    return Either.error(ex);
                }
            }

            @Override
            public Either<String> beginTransaction(IServerSessionId sessionId) {
                return getSession(sessionId).then(db -> {
                    ITransaction trans = db.db.getTransaction();
                    String transactionId = String.valueOf(transactionCount.getAndIncrement());
                    db.transactions.put(transactionId, trans);
                    return Either.ok(transactionId);
                });
            }

            @Override
            public Either<Void> endTransaction(IServerSessionId sessionId, String transactionId, boolean commit) {
                return getSession(sessionId).then(db -> {
                    if (transactionId == null) {
                        return Either.error("No transaction ID");
                    }
                    ITransaction transaction = db.transactions.remove(transactionId);
                    if (transaction == null) {
                        return Either.error("Transaction inactive: " + transactionId);
                    }
                    try {
                        if (commit) {
                            transaction.commit();
                        } else {
                            transaction.rollback();
                        }
                    } catch (SQLException ex) {
                        log(ex);
                        return Either.error(ex);
                    }
                    return Either.ok(null);
                });
            }

            @SuppressWarnings("unchecked")
            @Override
            public Either<Object> invoke(IServerSessionId sessionId, String transactionId, Method method, Object[] args) {
                return getSession(sessionId).then(db -> {
                    Class<? extends IDBCommon> iface = (Class<? extends IDBCommon>) method.getDeclaringClass();
                    Object impl;
                    if (transactionId != null) {
                        ITransaction transaction = db.transactions.get(transactionId);
                        if (transaction == null)
                            return Either.error("Transaction inactive: " + transactionId);
                        impl = transaction.getInterface(iface);
                    } else {
                        ISimpleTransaction t = db.db.getSimpleTransaction();
                        impl = t.getInterface(iface);
                    }
                    Object result = null;
                    Throwable error = null;
                    try {
                        result = method.invoke(impl, args);
                    } catch (InvocationTargetException itex) {
                        error = itex.getTargetException();
                    } catch (Throwable ex) {
                        log(ex);
                        error = new RemoteException(ex);
                    }
                    if (error != null) {
                        return Either.error(error);
                    } else {
                        return Either.ok(result);
                    }
                });
            }

            @Override
            public Either<Void> ping(IServerSessionId sessionId) {
                DBWrapper db = maybeSession(sessionId);
                if (db != null) {
                    db.db.ping();
                    if (LocalConnectionFactory.TRACE) {
                        lw.logger.trace(":: Ping from " + request.hostName());
                    }
                }
                return Either.ok(null);
            }

            @Override
            public Either<Void> close(IServerSessionId sessionId) {
                return getSession(sessionId).then(db -> {
                    db.db.close();
                    endSession(db.sessionId);
                    return Either.ok(null);
                });
            }
        };
    }

    /**
     * Dispatch of HTTP POST request.
     *
     * @param request HTTP request
     */
    public void dispatch(IHttpRequest request) throws IOException {
        request.perform(getInteraction(request));
    }

    /**
     * Server shutdown
     */
    public void shutdown() {
        watcher.shutdown();
    }
}
