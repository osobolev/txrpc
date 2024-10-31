package txrpc.remote.common.body;

import java.io.Serializable;

public final class HttpDBInterfaceInfo implements Serializable {

    public final Object id;
    public final Object userObject;

    public HttpDBInterfaceInfo(Object id, Object userObject) {
        this.id = id;
        this.userObject = userObject;
    }
}
