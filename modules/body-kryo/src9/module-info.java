module io.github.osobolev.txrpc.body.kryo {
    exports txrpc.remote.common.body.kryo;

    requires transitive com.esotericsoftware.kryo;
    requires transitive io.github.osobolev.txrpc.body.common;
}
