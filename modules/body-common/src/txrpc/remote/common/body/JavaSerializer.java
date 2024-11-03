package txrpc.remote.common.body;

import txrpc.remote.common.UnrecoverableRemoteException;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public final class JavaSerializer implements ISerializer {

    public static final class JavaWriter implements Writer {

        private final ObjectOutputStream oos;

        public JavaWriter(ObjectOutputStream oos) {
            this.oos = oos;
        }

        @Override
        public <T> void write(T obj, Class<T> cls) throws IOException {
            oos.writeObject(obj);
        }

        @Override
        public void close() throws IOException {
            oos.close();
        }
    }

    public static final class JavaReader implements Reader {

        private final ObjectInputStream ois;

        public JavaReader(ObjectInputStream ois) {
            this.ois = ois;
        }

        @Override
        public <T> T read(Class<T> cls) throws IOException {
            try {
                return cls.cast(ois.readObject());
            } catch (ClassNotFoundException | InvalidClassException ex) {
                throw new UnrecoverableRemoteException(ex);
            }
        }

        @Override
        public void close() throws IOException {
            ois.close();
        }
    }

    private final boolean gzip;

    public JavaSerializer(boolean gzip) {
        this.gzip = gzip;
    }

    public JavaSerializer() {
        this(false);
    }

    @Override
    public Writer newWriter(OutputStream os) throws IOException {
        OutputStream compressed = gzip ? new GZIPOutputStream(os) : os;
        return new JavaWriter(new ObjectOutputStream(compressed));
    }

    @Override
    public Reader newReader(InputStream is) throws IOException {
        InputStream decompressed = gzip ? new GZIPInputStream(is) : is;
        return new JavaReader(new ObjectInputStream(decompressed));
    }
}
