package txrpc.remote.client;

import txrpc.api.IDBCommon;
import txrpc.api.IDBInterface;
import txrpc.api.ISimpleTransaction;
import txrpc.api.ITransaction;
import txrpc.remote.common.IRemoteDBInterface;
import txrpc.remote.common.RemoteException;
import txrpc.remote.common.UnrecoverableRemoteException;
import txrpc.remote.common.WatcherThread;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.function.Consumer;

/**
 * Wrapper for {@link IDBInterface} on the client side.
 * Caches transactions - creates only one transaction per thread.
 * Also runs activity notification thread to notify server about this client.
 */
public final class SafeDBInterface implements IRemoteDBInterface {

    private IRemoteDBInterface idb;
    private final ConnectionProducer producer;
    private int resetCounter = 0;
    private boolean unrecoverable = false;

    private final Object dbLock = new Object();
    private final WatcherThread watcher;

    public SafeDBInterface(Consumer<Throwable> logger, ConnectionProducer producer) throws Exception {
        this(logger, producer.open(), producer);
    }

    /**
     * Constructor.
     *
     * @param idb DB connection
     */
    public SafeDBInterface(Consumer<Throwable> logger, IRemoteDBInterface idb) {
        this(logger, idb, null);
    }

    public SafeDBInterface(Consumer<Throwable> logger, IRemoteDBInterface idb, ConnectionProducer producer) {
        this.idb = idb;
        this.producer = producer;
        // pinging twice as frequent as server checks session activity
        this.watcher = new WatcherThread(2, () -> {
            try {
                ping();
            } catch (RemoteException ex) {
                logger.accept(ex);
            }
        });
        this.watcher.runThread();
    }

    private IRemoteDBInterface getDb() throws Exception {
        synchronized (dbLock) {
            if (idb == null && producer != null) {
                if (unrecoverable)
                    throw new RemoteException("Unrecoverable error, please restart application");
                idb = producer.open();
            }
            return idb;
        }
    }

    ISimpleTransaction createSimpleTransaction() throws SQLException {
        try {
            return getDb().getSimpleTransaction();
        } catch (RemoteException | SQLException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RemoteException(ex);
        }
    }

    @Override
    public ISimpleTransaction getSimpleTransaction() throws SQLException {
        if (producer == null) {
            return createSimpleTransaction();
        } else {
            return new SafeSimpleTransaction(this);
        }
    }

    @Override
    public ITransaction getTransaction() throws SQLException {
        try {
            return getDb().getTransaction();
        } catch (RemoteException | SQLException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RemoteException(ex);
        }
    }

    @Override
    public void ping() {
        try {
            getDb().ping();
        } catch (RemoteException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RemoteException(ex);
        }
    }

    @Override
    public void close() throws SQLException {
        watcher.shutdown();
        synchronized (dbLock) {
            if (idb != null) {
                idb.close();
                idb = null;
            }
        }
    }

    <T extends IDBCommon> T wrap(Class<T> iface, SafeWrapper<T> obj) {
        return iface.cast(Proxy.newProxyInstance(iface.getClassLoader(), new Class<?>[] {iface}, (proxy, method, args) -> {
            try {
                return method.invoke(obj.get(), args);
            } catch (InvocationTargetException itex) {
                Throwable ex = itex.getTargetException();
                resetConnection(ex instanceof UnrecoverableRemoteException);
                throw ex;
            }
        }));
    }

    private void resetConnection(boolean unrecoverable) {
        synchronized (dbLock) {
            if (idb != null) {
                try {
                    idb.close();
                } catch (SQLException | RemoteException ex) {
                    // ignore
                }
                idb = null;
                resetCounter++;
                this.unrecoverable = unrecoverable;
            }
        }
    }

    int getResetCounter() {
        synchronized (dbLock) {
            return resetCounter;
        }
    }
}
