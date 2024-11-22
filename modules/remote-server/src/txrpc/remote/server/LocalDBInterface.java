package txrpc.remote.server;

import txrpc.remote.common.IRemoteDBInterface;
import txrpc.runtime.SessionContext;
import txrpc.runtime.TxRpcGlobalContext;

final class LocalDBInterface extends DBInterface implements IRemoteDBInterface {

    LocalDBInterface(SessionContext session, TxRpcGlobalContext global, TxRpcLogger logger, long sessionOrderId) {
        super(session, global, logger, sessionOrderId);
    }

    protected String getConnectionName() {
        return "local connection";
    }

    @Override
    public void ping() {
    }
}
