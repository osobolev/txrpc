module io.github.osobolev.txrpc.remote.kryo {
    exports txrpc.remote.kryo;

    requires transitive com.esotericsoftware.kryo;
    requires transitive io.github.osobolev.txrpc.remote.common;
}
