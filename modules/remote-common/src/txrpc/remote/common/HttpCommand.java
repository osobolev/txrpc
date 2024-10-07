package txrpc.remote.common;

public enum HttpCommand {
    OPEN,
    GET_TRANSACTION,
    PING,
    CLOSE,
    ROLLBACK,
    COMMIT,
    INVOKE
}
