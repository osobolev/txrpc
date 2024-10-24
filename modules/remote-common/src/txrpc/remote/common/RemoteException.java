package txrpc.remote.common;

public class RemoteException extends RuntimeException {

    public RemoteException(String message) {
        super(message);
    }

    public RemoteException(Throwable cause) {
        super(cause.getMessage(), cause);
    }

    public RemoteException(String message, Throwable cause) {
        super(message, cause);
    }
}
