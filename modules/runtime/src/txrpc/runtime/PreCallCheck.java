package txrpc.runtime;

import java.lang.reflect.Method;

public interface PreCallCheck {

    void check(Method method, Object[] args);
}
