module io.github.osobolev.txrpc.body.client {
    exports txrpc.remote.client.body;

    requires transitive io.github.osobolev.txrpc.body.common;
    requires transitive io.github.osobolev.txrpc.remote.client;
}
