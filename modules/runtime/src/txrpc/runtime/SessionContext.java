package txrpc.runtime;

import java.sql.SQLException;

public final class SessionContext {

    final ConnectionManager cman;
    private final Object userObject;
    final PreCallCheck beforeCall;

    public SessionContext(ConnectionManager cman, Object userObject, PreCallCheck beforeCall) {
        this.cman = cman;
        this.userObject = userObject;
        this.beforeCall = beforeCall;
    }

    void close() throws SQLException {
        cman.close();
    }

    Object getUserObject() {
        return userObject;
    }

    public static class Builder {

        private Object userObject = null;
        private PreCallCheck beforeCall = null;

        public Builder setUserObject(Object userObject) {
            this.userObject = userObject;
            return this;
        }

        public Builder setBeforeCall(PreCallCheck beforeCall) {
            this.beforeCall = beforeCall;
            return this;
        }

        public SessionContext build(ConnectionManager cman) {
            return new SessionContext(cman, userObject, beforeCall);
        }
    }
}
