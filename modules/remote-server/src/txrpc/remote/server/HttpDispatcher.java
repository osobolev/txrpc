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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
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

        final DBInterface db;
        final AtomicInteger transactionCount = new AtomicInteger(0);
        final ConcurrentMap<String, ITransaction> transactions = new ConcurrentHashMap<>();
        final AtomicLong lastActive = new AtomicLong(getCurrentTime());

        DBWrapper(DBInterface db) {
            this.db = db;
        }

        void ping() {
            lastActive.set(getCurrentTime());
        }

        boolean isTimedOut(long time) {
            return time - lastActive.get() >= WatcherThread.ACTIVITY_CHECK_INTERVAL;
        }
    }

    private final Map<String, DBWrapper> connectionMap = new HashMap<>();

    public HttpDispatcher(SessionFactory sessionFactory, TxRpcLogger logger, TxRpcGlobalContext global) {
        this.lw = new LocalConnectionFactory(sessionFactory, logger, global);
        this.watcher = new WatcherThread(1, this::checkActivity);
        this.watcher.runThread();
    }

    private static long getCurrentTime() {
        return System.currentTimeMillis();
    }

    private void log(Throwable ex) {
        lw.logger.error(ex);
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
        long time = getCurrentTime();
        List<DBWrapper> toClose = null;
        synchronized (connectionMap) {
            Iterator<DBWrapper> it = connectionMap.values().iterator();
            while (it.hasNext()) {
                DBWrapper db = it.next();
                if (db.isTimedOut(time)) {
                    if (toClose == null) {
                        toClose = new ArrayList<>();
                    }
                    toClose.add(db);
                    it.remove();
                }
            }
        }
        if (toClose != null) {
            for (DBWrapper db : toClose) {
                try {
                    db.db.close(false);
                } catch (Throwable ex) {
                    log(ex);
                }
            }
        }
    }

    private TxRpcInteraction<IServerSessionId> getInteraction(IHttpRequest request) {
        return new TxRpcInteraction<IServerSessionId>() {

            private DBWrapper getSession(IServerSessionId sessionId) {
                if (sessionId == null) {
                    throw new RemoteException("No session ID");
                }
                DBWrapper db = maybeSession(sessionId);
                if (db == null) {
                    throw new RemoteException("Session closed");
                }
                return db;
            }

            @Override
            public Either<NewSession<IServerSessionId>> open(String user, String password) {
                try {
                    NewSession<IServerSessionId> session = lw.openConnection(
                        user, password, request.hostName(), true,
                        db -> {
                            IServerSessionId id = request.newSessionId();
                            putConnection(id, new DBWrapper(db));
                            return new NewSession<>(id, db.getUserObject());
                        }
                    );
                    return Either.ok(session);
                } catch (SQLException ex) {
                    log(ex);
                    return Either.error(ex);
                }
            }

            @Override
            public Either<String> beginTransaction(IServerSessionId sessionId) {
                DBWrapper db = getSession(sessionId);
                ITransaction trans = db.db.getTransaction();
                String transactionId = String.valueOf(db.transactionCount.getAndIncrement());
                db.transactions.put(transactionId, trans);
                return Either.ok(transactionId);
            }

            @Override
            public Either<Void> endTransaction(IServerSessionId sessionId, String transactionId, boolean commit) {
                DBWrapper db = getSession(sessionId);
                if (transactionId == null) {
                    throw new RemoteException("No transaction ID");
                }
                ITransaction transaction = db.transactions.remove(transactionId);
                if (transaction == null) {
                    throw new RemoteException("Transaction inactive: " + transactionId);
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
            }

            @SuppressWarnings("unchecked")
            @Override
            public Either<Object> invoke(IServerSessionId sessionId, String transactionId, Method method, Object[] args) {
                DBWrapper db = getSession(sessionId);
                ISimpleTransaction t;
                if (transactionId != null) {
                    ITransaction transaction = db.transactions.get(transactionId);
                    if (transaction == null)
                        throw new RemoteException("Transaction inactive: " + transactionId);
                    t = transaction;
                } else {
                    t = db.db.getSimpleTransaction();
                }
                Class<? extends IDBCommon> iface = (Class<? extends IDBCommon>) method.getDeclaringClass();
                Object impl = t.getInterface(iface);
                try {
                    Object result = method.invoke(impl, args);
                    return Either.ok(result);
                } catch (InvocationTargetException itex) {
                    Throwable error = itex.getTargetException();
                    log(error);
                    return Either.error(error);
                } catch (Throwable ex) {
                    log(ex);
                    throw new RemoteException("Cannot invoke method", ex);
                }
            }

            @Override
            public Either<Void> ping(IServerSessionId sessionId) {
                DBWrapper db = maybeSession(sessionId);
                if (db != null) {
                    db.ping();
                    if (LocalConnectionFactory.TRACE) {
                        lw.logger.trace(":: Ping from " + request.hostName());
                    }
                }
                return Either.ok(null);
            }

            @Override
            public Either<Void> close(IServerSessionId sessionId) {
                DBWrapper db = getSession(sessionId);
                db.db.close();
                endSession(sessionId);
                return Either.ok(null);
            }
        };
    }

    /**
     * Dispatch of HTTP POST request.
     *
     * @param request HTTP request
     */
    public void dispatch(IHttpRequest request) throws IOException {
        try {
            request.perform(getInteraction(request));
        } catch (RemoteException ex) {
            request.writeError(ex);
        } catch (RuntimeException | Error ex) {
            log(ex);
            throw ex;
        }
    }

    /**
     * Server shutdown
     */
    public void shutdown() {
        watcher.shutdown();
    }
}
