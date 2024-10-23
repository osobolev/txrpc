package txrpc.remote.client;

import txrpc.api.IDBCommon;
import txrpc.api.ISimpleTransaction;
import txrpc.remote.common.Either;
import txrpc.remote.common.TxRpcInteraction;

import java.io.IOException;
import java.lang.reflect.Proxy;

class HttpSimpleTransaction implements ISimpleTransaction {

    protected final TxRpcInteraction<IClientSessionId> interaction;
    protected final IClientSessionId sessionId;

    HttpSimpleTransaction(TxRpcInteraction<IClientSessionId> interaction, IClientSessionId sessionId) {
        this.interaction = interaction;
        this.sessionId = sessionId;
    }

    protected String getTransactionId() {
        return null;
    }

    @Override
    public final <T extends IDBCommon> T getInterface(Class<T> iface) {
        return iface.cast(Proxy.newProxyInstance(
            iface.getClassLoader(),
            new Class<?>[] {iface},
            (proxy, method, args) -> {
                Either<Object> result;
                try {
                    result = interaction.invoke(sessionId, getTransactionId(), method, args);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                return result.rethrow(method);
            }
        ));
    }
}
