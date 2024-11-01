package txrpc.runtime;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Data source implementation of {@link ConnectionManager}. Can be used inside JavaEE containers
 * with container-managed transactions (commit and rollback are not invoked by default, transaction management
 * is relegated to container).
 */
public class DataSourceConnectionManager implements ConnectionManager {

    protected final DataSource dataSource;

    public DataSourceConnectionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Connection allocConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void releaseConnection(Connection conn) throws SQLException {
        conn.close();
    }

    /**
     * Does nothing. Override if you do not use container-managed transactions.
     */
    @Override
    public void commit(Connection conn) throws SQLException {
    }

    /**
     * Does nothing. Override if you do not use container-managed transactions.
     */
    @Override
    public void rollback(Connection conn) throws SQLException {
    }

    @Override
    public void close() {
    }
}
