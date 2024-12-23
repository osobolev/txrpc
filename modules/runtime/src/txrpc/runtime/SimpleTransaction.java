package txrpc.runtime;

import txrpc.api.IDBCommon;
import txrpc.api.ISimpleTransaction;

final class SimpleTransaction implements ISimpleTransaction {

    private final TransactionContext transaction;

    SimpleTransaction(TxRpcGlobalContext global, SessionContext session) {
        this.transaction = new TransactionContext(global, session, true);
    }

    @Override
    public <T extends IDBCommon> T getInterface(Class<T> iface) {
        return transaction.getInterface(iface);
    }
}
