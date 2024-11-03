package txrpc.remote.common.body.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.JavaSerializer;
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy;
import org.objenesis.strategy.StdInstantiatorStrategy;
import txrpc.remote.common.UnrecoverableRemoteException;
import txrpc.remote.common.body.ISerializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public final class KryoSerializer implements ISerializer {

    public static final class KryoWriter implements Writer {

        private final Kryo kryo;
        private final Output output;

        public KryoWriter(Kryo kryo, Output output) {
            this.kryo = kryo;
            this.output = output;
        }

        @Override
        public <T> void write(T obj, Class<T> cls) {
            try {
                kryo.writeObject(output, obj);
            } catch (KryoException ex) {
                throw new UnrecoverableRemoteException(ex);
            }
        }

        @Override
        public void close() {
            try {
                output.close();
            } catch (KryoException ex) {
                throw new UnrecoverableRemoteException(ex);
            }
        }
    }

    public static final class KryoReader implements Reader {

        private final Kryo kryo;
        private final Input input;

        public KryoReader(Kryo kryo, Input input) {
            this.kryo = kryo;
            this.input = input;
        }

        @Override
        public <T> T read(Class<T> cls) {
            try {
                return kryo.readObject(input, cls);
            } catch (KryoException ex) {
                throw new UnrecoverableRemoteException(ex);
            }
        }

        @Override
        public void close() {
            try {
                input.close();
            } catch (KryoException ex) {
                throw new UnrecoverableRemoteException(ex);
            }
        }
    }

    private final boolean gzip;
    private Consumer<Kryo> kryoCustomizer = KryoSerializer::setupKryo;

    private final ThreadLocal<Kryo> kryos = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryoCustomizer.accept(kryo);
        return kryo;
    });

    public KryoSerializer(boolean gzip) {
        this.gzip = gzip;
    }

    public KryoSerializer() {
        this(false);
    }

    public static void setupKryo(Kryo kryo) {
        kryo.setRegistrationRequired(false);
        kryo.setInstantiatorStrategy(new DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
        kryo.addDefaultSerializer(Throwable.class, new JavaSerializer());
    }

    public Consumer<Kryo> getKryoCustomizer() {
        return kryoCustomizer;
    }

    public void setKryoCustomizer(Consumer<Kryo> kryoCustomizer) {
        this.kryoCustomizer = kryoCustomizer;
    }

    public Kryo getKryo() {
        return kryos.get();
    }

    @Override
    public Writer newWriter(OutputStream os) throws IOException {
        OutputStream compresssed = gzip ? new GZIPOutputStream(os) : os;
        return new KryoWriter(getKryo(), new Output(compresssed));
    }

    @Override
    public Reader newReader(InputStream is) throws IOException {
        InputStream decompressed = gzip ? new GZIPInputStream(is) : is;
        return new KryoReader(getKryo(), new Input(decompressed));
    }
}
