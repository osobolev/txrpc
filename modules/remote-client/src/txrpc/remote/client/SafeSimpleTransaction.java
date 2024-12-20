package txrpc.remote.client;

import txrpc.api.IDBCommon;
import txrpc.api.ISimpleTransaction;
import txrpc.remote.common.RemoteException;

import java.sql.SQLException;

final class SafeSimpleTransaction implements ISimpleTransaction {

    private ISimpleTransaction trans = null;
    private int currentCounter = -1;
    private final SafeDBInterface db;
    private final Object dbLock = new Object();

    SafeSimpleTransaction(SafeDBInterface db) {
        this.db = db;
    }

    private ISimpleTransaction getTrans() {
        synchronized (dbLock) {
            int dbCounter = db.getResetCounter();
            if (dbCounter != currentCounter) {
                trans = null;
                currentCounter = dbCounter;
            }
            if (trans == null) {
                try {
                    trans = db.createSimpleTransaction();
                } catch (SQLException ex) {
                    throw new RemoteException(ex);
                }
            }
            return trans;
        }
    }

    @Override
    public <T extends IDBCommon> T getInterface(Class<T> iface) {
        return db.wrap(iface, new SafeWrapper<>(iface, db, this));
    }

    <T extends IDBCommon> T createInterface(Class<T> iface) {
        return getTrans().getInterface(iface);
    }
}
