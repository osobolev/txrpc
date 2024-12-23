package txrpc.runtime;

import txrpc.api.IDBCommon;
import txrpc.api.ISimpleTransaction;

import java.sql.Connection;

/**
 * This class can be used to call transactional code from plain JDBC connection. If you have {@link Connection} instance then
 * you can retrieve business interface <code>ITest</code> in the following way:
 * <pre>
 * Connection conn = ...;
 * ISimpleTransaction trans = new JdbcInterface(global, conn, false);
 * ITest iface = trans.getInterface(ITest.class);
 * </pre>
 */
public final class JdbcInterface implements ISimpleTransaction {

    private final TransactionContext transaction;

    public JdbcInterface(TxRpcGlobalContext global, SessionContext session, boolean commitCalls) {
        this.transaction = new TransactionContext(global, session, commitCalls);
    }

    public JdbcInterface(TxRpcGlobalContext global, SessionContext session) {
        this(global, session, false);
    }

    @Override
    public <T extends IDBCommon> T getInterface(Class<T> iface) {
        return transaction.getInterface(iface);
    }

    public static SessionContext jdbcContext(SessionContext.Builder builder, Connection connection) {
        return builder.build(new SingleConnectionManager(connection));
    }
}
