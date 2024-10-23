package txrpc.remote.common;

import java.io.IOException;
import java.lang.reflect.Method;

public interface TxRpcInteraction<S> {

    final class NewSession<S> {

        public final S sessionId;
        public final Object userObject;

        public NewSession(S sessionId, Object userObject) {
            this.sessionId = sessionId;
            this.userObject = userObject;
        }
    }

    Either<NewSession<S>> open(String user, String password) throws IOException;

    Either<String> beginTransaction(S sessionId) throws IOException;

    Either<Void> endTransaction(S sessionId, String transactionId, boolean commit) throws IOException;

    Either<Object> invoke(S sessionId, String transactionId,
                          Method method, Object[] args) throws IOException;

    Either<Void> ping(S sessionId) throws IOException;

    Either<Void> close(S sessionId) throws IOException;
}
