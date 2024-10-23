module io.github.osobolev.txrpc.body.server {
    exports txrpc.remote.server.body;

    requires transitive io.github.osobolev.txrpc.body.common;
    requires transitive io.github.osobolev.txrpc.remote.server;
}
