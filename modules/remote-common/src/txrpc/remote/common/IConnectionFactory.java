package txrpc.remote.common;

import txrpc.api.IDBInterface;

import java.sql.SQLException;

/**
 * Root interface for DB objects hierarchy - all other objects are produced
 * by IConnectionFactory.
 */
public interface IConnectionFactory {

    /**
     * Connecting to DB using authentication.
     *
     * @param user external user
     * @param password external password
     * @return DB connection
     */
    IDBInterface openConnection(String user, String password) throws SQLException;
}
