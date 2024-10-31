package txrpc.remote.server.body;

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

    public final HttpRequest requestData() throws IOException {
        try (ISerializer.Reader fromClient = serializer.newReader(in)) {
            return fromClient.read(HttpRequest.class);
        }
    }

    public final void write(HttpResult result) throws IOException {
        try (ISerializer.Writer toClient = serializer.newWriter(out)) {
            toClient.write(result, HttpResult.class);
        }
    }
}
