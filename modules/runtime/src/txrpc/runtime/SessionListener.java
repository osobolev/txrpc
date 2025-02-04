package txrpc.runtime;

public interface SessionListener {

    void opened(long sessionId, String user, String host, Object userObject);

    void closed(long sessionId);
}
