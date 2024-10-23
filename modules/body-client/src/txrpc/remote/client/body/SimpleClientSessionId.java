package txrpc.remote.client.body;

import txrpc.remote.client.IClientSessionId;

final class SimpleClientSessionId implements IClientSessionId {

    final String id;

    SimpleClientSessionId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }
}
