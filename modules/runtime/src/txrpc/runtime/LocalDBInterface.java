package txrpc.runtime;

import txrpc.api.IDBInterface;
import txrpc.api.ISimpleTransaction;
import txrpc.api.ITransaction;

import java.sql.SQLException;

public final class LocalDBInterface implements IDBInterface {

    private final TxRpcGlobalContext global;
    private final SessionContext session;

    public LocalDBInterface(TxRpcGlobalContext global, SessionContext session) {
        this.global = global;
        this.session = session;
    }

    @Override
    public ISimpleTransaction getSimpleTransaction() {
        return new SimpleTransaction(global, session);
    }

    @Override
    public ITransaction getTransaction() {
        return new Transaction(global, session);
    }

    @Override
    public void close() throws SQLException {
        session.close();
    }
}
