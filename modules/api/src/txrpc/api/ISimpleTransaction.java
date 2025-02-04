package txrpc.api;

/**
 * "Simple" transaction. "Simple" transaction makes method calls atomic,
 * every business method call made from a simple transaction either commits or rolls back
 * (the latter in the case of exception).
 * In the most cases simple transactions are sufficient. For user-managed transactions
 * (where you should explicitly call commit or rollback) use {@link ITransaction}.
 * Unlike {@link ITransaction}, ISimpleTransaction does not hold any resources between method calls.
 */
public interface ISimpleTransaction {

    /**
     * Returns data access interface generated by preprocessor.
     *
     * @param iface interface class
     * @return data access interface implementation
     */
    <T extends IDBCommon> T getInterface(Class<T> iface);
}
