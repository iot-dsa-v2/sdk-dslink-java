package org.iot.dsa.dslink.requester;

import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;

/**
 * Parameter to invoke method on DSIRequester.  Provides callbacks for various state changes.
 *
 * @author Daniel Shapiro, Aaron Hansen
 */
public interface OutboundInvokeHandler extends OutboundRequestHandler {

    public void onColumns(DSList list);

    /**
     * Called by the requester before returning from the invoke method.
     *
     * @param path   Path being listed.
     * @param params Parameter to the invoke method.
     * @param stub Mechanism to close the request stream.
     */
    public void onInit(String path, DSMap params, OutboundRequestStub stub);

    public void onInsert(int index, DSList rows);

    public void onMode(Mode mode);

    public void onReplace(int start, int end, DSList rows);

    public void onTableMeta(DSMap map);

    public void onUpdate(DSList row);

    public enum Mode {
        APPEND,
        REFRESH,
        STREAM,
    }
}
