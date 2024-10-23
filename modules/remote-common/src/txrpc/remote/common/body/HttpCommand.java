package txrpc.remote.common.body;

public enum HttpCommand {
    OPEN,
    GET_TRANSACTION,
    PING,
    CLOSE,
    ROLLBACK,
    COMMIT,
    INVOKE
}
