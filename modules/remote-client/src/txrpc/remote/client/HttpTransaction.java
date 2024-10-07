package txrpc.remote.client;

import txrpc.api.ITransaction;
import txrpc.remote.common.HttpCommand;
import txrpc.remote.common.HttpId;
import txrpc.remote.common.RemoteException;

import java.sql.SQLException;

final class HttpTransaction extends HttpSimpleTransaction implements ITransaction {

    HttpTransaction(HttpRootObject rootObject, HttpId id, Object clientContext) {
        super(rootObject, id, clientContext, HttpCommand.INVOKE);
    }

    public void rollback() throws SQLException {
        try {
            rootObject.httpInvoke(void.class, clientContext, HttpCommand.ROLLBACK, id);
        } catch (SQLException | RuntimeException ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new RemoteException(ex);
        }
    }

    public void commit() throws SQLException {
        try {
            rootObject.httpInvoke(void.class, clientContext, HttpCommand.COMMIT, id);
        } catch (SQLException | RuntimeException ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new RemoteException(ex);
        }
    }
}
