package txrpc.remote.server.body;

import txrpc.remote.common.body.HttpId;
import txrpc.remote.common.body.HttpRequest;
import txrpc.remote.common.body.HttpResult;
import txrpc.remote.common.body.ISerializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class BaseServerBodyInteraction implements IBodyInteraction {

    private final ISerializer serializer;
    private final InputStream in;
    private final OutputStream out;

    protected BaseServerBodyInteraction(ISerializer serializer, InputStream in, OutputStream out) {
        this.serializer = serializer;
        this.in = in;
        this.out = out;
    }

    protected abstract ServerHttpId serverId(HttpId id);

    public final ServerHttpRequest requestData() throws IOException {
        HttpRequest request;
        try (ISerializer.Reader fromClient = serializer.newReader(in)) {
            request = fromClient.read(HttpRequest.class);
        }
        return new ServerHttpRequest(
            serverId(request.id), request.getCommand(),
            request.iface, request.method, request.paramTypes, request.params
        );
    }

    public final void write(HttpResult result) throws IOException {
        try (ISerializer.Writer toClient = serializer.newWriter(out)) {
            toClient.write(result, HttpResult.class);
        }
    }
}
