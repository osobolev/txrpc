package txrpc.runtime;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.Semaphore;

/**
 * Single-connection implementation of {@link ConnectionManager}.
 * This class is thread-safe and when one thread is using connection
 * others have to wait.
 */
public class SingleConnectionManager implements ConnectionManager {

    private final Connection conn;
    private final Semaphore available = new Semaphore(1);

    public SingleConnectionManager(Connection conn) {
        this.conn = conn;
    }

    public static Connection openConnection(String driver, String url, String user, String pass) throws SQLException {
        if (driver != null) {
            try {
                Class.forName(driver);
            } catch (ClassNotFoundException ex) {
                throw new SQLException(ex);
            }
        }
        Connection conn = DriverManager.getConnection(url, user, pass);
        try {
            conn.setAutoCommit(false);
        } catch (SQLException ex) {
            try {
                conn.close();
            } catch (SQLException ex2) {
                ex.addSuppressed(ex2);
            }
            throw ex;
        }
        return conn;
    }

    public static Connection openConnection(String url, String user, String pass) throws SQLException {
        return openConnection(null, url, user, pass);
    }

    @Override
    public Connection allocConnection() throws SQLException {
        try {
            available.acquire();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new SQLException(ex);
        }
        return conn;
    }

    @Override
    public void releaseConnection(Connection conn) {
        available.release();
    }

    @Override
    public void commit(Connection conn) throws SQLException {
        conn.commit();
    }

    @Override
    public void rollback(Connection conn) throws SQLException {
        conn.rollback();
    }

    @Override
    public void close() throws SQLException {
        conn.close();
    }
}
