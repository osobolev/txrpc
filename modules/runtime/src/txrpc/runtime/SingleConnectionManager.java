package txrpc.runtime;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Single-connection implementation of {@link ConnectionManager}.
 * This class is thread-safe and when one thread is using connection
 * others have to wait.
 */
public class SingleConnectionManager implements ConnectionManager {

    private final Connection conn;
    private final Object lock = new Object();
    private boolean allocated = false;

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

    public Connection allocConnection() throws SQLException {
        synchronized (lock) {
            while (allocated) {
                try {
                    lock.wait();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new SQLException(ex);
                }
            }
            this.allocated = true;
        }
        return conn;
    }

    public void releaseConnection(Connection conn) {
        synchronized (lock) {
            this.allocated = false;
            lock.notifyAll();
        }
    }

    public void commit(Connection conn) throws SQLException {
        conn.commit();
    }

    public void rollback(Connection conn) throws SQLException {
        conn.rollback();
    }

    public void close() throws SQLException {
        conn.close();
    }
}
