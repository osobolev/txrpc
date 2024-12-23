package txrpc.runtime;

import txrpc.api.IDBCommon;
import txrpc.api.ITransaction;

import java.sql.SQLException;

final class Transaction implements ITransaction {

    private final TransactionContext transaction;

    Transaction(TxRpcGlobalContext global, SessionContext session) {
        this.transaction = new TransactionContext(global, session, false);
    }

    @Override
    public <T extends IDBCommon> T getInterface(Class<T> iface) {
        return transaction.getInterface(iface);
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
