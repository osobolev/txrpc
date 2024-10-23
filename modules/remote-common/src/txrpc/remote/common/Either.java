package txrpc.remote.common;

import java.lang.reflect.Method;

public final class Either<T> {

    private final T result;
    private final Throwable error;

    private Either(T result, Throwable error) {
        this.result = result;
        this.error = error;
    }

    public static <T> Either<T> ok(T result) {
        return new Either<>(result, null);
    }

    public static <T> Either<T> error(Throwable error) {
        return new Either<>(null, error);
    }

    public static <T> Either<T> error(String error) {
        return error(new RemoteException(error));
    }

    public T getResult() {
        return result;
    }

    public Throwable getError() {
        return error;
    }

    public interface ErrorChecker<E extends Throwable> {

        E check(Throwable ex);
    }

    public <E extends Throwable> T rethrow(ErrorChecker<E> checker) throws E {
        if (error != null) {
            if (error instanceof RuntimeException) {
                throw (RuntimeException) error;
            } else if (error instanceof Error) {
                throw (Error) error;
            } else {
                E allowed = checker.check(error);
                if (allowed != null) {
                    throw allowed;
                } else {
                    throw new RemoteException("Unexpected exception: " + error.getClass(), error);
                }
            }
        } else {
            return result;
        }
    }

    public T rethrow(Method method) throws Throwable {
        return rethrow(ex -> {
            for (Class<?> allowed : method.getExceptionTypes()) {
                if (allowed.isInstance(ex)) {
                    return ex;
                }
            }
            return null;
        });
    }

    public <E extends Throwable> T rethrow(Class<E> allowed) throws E {
        return rethrow(ex -> allowed.isInstance(ex) ? allowed.cast(ex) : null);
    }

    public interface Mapper<T, U, E extends Exception> {

        U map(T value) throws E;
    }

    public <U, E extends Exception> Either<U> then(Mapper<T, Either<U>, E> f) throws E {
        if (error != null) {
            return error(error);
        }
        return f.map(result);
    }

    public <U, E extends Exception> Either<U> map(Mapper<T, U, E> f) throws E {
        return then(value -> ok(f.map(value)));
    }
}
