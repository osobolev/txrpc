package txrpc.remote.server;

public interface TxRpcLogger {

    void trace(String message);

    void info(String message);

    void error(String message);

    void error(Throwable error);

    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    class Simple implements TxRpcLogger {

        @Override
        public void trace(String message) {
            System.out.println(message);
        }

        @Override
        public void info(String message) {
            System.out.println(message);
        }

        @Override
        public void error(String message) {
            System.err.println(message);
        }

        @Override
        public void error(Throwable error) {
            error.printStackTrace(System.err);
        }
    }
}
