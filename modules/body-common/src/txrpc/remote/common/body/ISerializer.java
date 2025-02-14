package txrpc.remote.common.body;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface ISerializer {

    interface Writer extends Closeable {

        void writeStreamIndex(int index) throws IOException;

        <T> void write(T obj, Class<T> cls) throws IOException;
    }

    interface Reader extends Closeable {

        int readStreamIndex() throws IOException;

        <T> T read(Class<T> cls) throws IOException;
    }

    Writer newWriter(OutputStream os) throws IOException;

    Reader newReader(InputStream is) throws IOException;
}
