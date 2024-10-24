package txrpc.runtime;

import txrpc.api.Impl;
import txrpc.api.TxCallException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

public final class TxRpcGlobalContext {

    final ImplConstructor toImpl;

    private final ConcurrentMap<Class<?>, Class<?>> implCache = new ConcurrentHashMap<>();

    public TxRpcGlobalContext(ImplConstructor toImpl) {
        this.toImpl = toImpl;
    }

    private static Class<?> getImplClass(Class<?> iface) {
        Class<Impl> annotationClass = Impl.class;
        Impl implAnnotation = iface.getAnnotation(annotationClass);
        if (implAnnotation == null)
            throw new TxCallException("No " + annotationClass.getCanonicalName() + " annotation found");
        try {
            return iface.getClassLoader().loadClass(implAnnotation.value());
        } catch (ClassNotFoundException ex) {
            throw new TxCallException("Cannot find implementation for " + iface.getCanonicalName());
        }
    }

    Class<?> getImpl(Class<?> iface) {
        return implCache.computeIfAbsent(iface, TxRpcGlobalContext::getImplClass);
    }

    private final List<SessionListener> sessionListeners = new ArrayList<>();

    public void addSessionListener(SessionListener listener) {
        synchronized (sessionListeners) {
            sessionListeners.add(listener);
        }
    }

    public void removeSessionListener(SessionListener listener) {
        synchronized (sessionListeners) {
            sessionListeners.remove(listener);
        }
    }

    public void fireSessionListeners(Consumer<SessionListener> consumer) {
        synchronized (sessionListeners) {
            for (SessionListener listener : sessionListeners) {
                consumer.accept(listener);
            }
        }
    }
}
