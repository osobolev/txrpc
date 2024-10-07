package txrpc.runtime;

import txrpc.api.IDBCommon;
import txrpc.api.ITransaction;

import java.sql.SQLException;

public final class Transaction implements ITransaction {

    private final TransactionContext transaction;

    public Transaction(TxRpcGlobalContext global, SessionContext session) {
        this.transaction = new TransactionContext(global, session);
    }

    @Override
    public <T extends IDBCommon> T getInterface(Class<T> iface) {
        return transaction.getInterface(iface, false);
    }

    @Override
    public void commit() throws SQLException {
        transaction.commit();
    }

    @Override
    public void rollback() throws SQLException {
        transaction.rollback();
    }
}
