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

    @Override
    public <T extends IDBCommon> T getInterface(Class<T> iface) {
        return transaction.getInterface(iface);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private boolean commitCalls = false;
        private Object userObject = null;
        private PreCallCheck beforeCall = null;

        public Builder setCommitCalls(boolean commitCalls) {
            this.commitCalls = commitCalls;
            return this;
        }

        public Builder setUserObject(Object userObject) {
            this.userObject = userObject;
            return this;
        }

        public Builder setBeforeCall(PreCallCheck beforeCall) {
            this.beforeCall = beforeCall;
            return this;
        }

        public JdbcInterface build(TxRpcGlobalContext global, Connection connection) {
            SingleConnectionManager cman = new SingleConnectionManager(connection);
            SessionContext session = new SessionContext(cman, userObject, beforeCall);
            return new JdbcInterface(global, session, commitCalls);
        }
    }
}
