module io.github.osobolev.txrpc.remote.server {
    exports txrpc.remote.server;
    exports txrpc.remote.server.body;

    requires transitive io.github.osobolev.txrpc.runtime;
    requires transitive io.github.osobolev.txrpc.remote.common;
}
