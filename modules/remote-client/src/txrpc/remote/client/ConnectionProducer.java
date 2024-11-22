package txrpc.remote.client;

import txrpc.api.IDBInterface;

/**
 * Used for client reconnects.
 */
public interface ConnectionProducer {

    IDBInterface open() throws Exception;
}
