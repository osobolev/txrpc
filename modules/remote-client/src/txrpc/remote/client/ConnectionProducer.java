package txrpc.remote.client;

import txrpc.remote.common.IRemoteDBInterface;

/**
 * Used for client reconnects.
 */
public interface ConnectionProducer {

    IRemoteDBInterface open() throws Exception;
}
