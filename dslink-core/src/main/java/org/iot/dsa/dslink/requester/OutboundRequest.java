package org.iot.dsa.dslink.requester;

import org.iot.dsa.node.DSElement;

/**
 * Data and callbacks common to all outbound requests.
 *
 * @author Daniel Shapiro, Aaron Hansen
 */
public class OutboundRequest {

    private String path;

    public String getPath() {
        return path;
    }

    /**
     * Callback for when the request stream is closed, no matter by who or how it is closed.  Does
     * nothing by default.
     */
    public void onClose() {
    }

    /**
     * Callback for when an error is received.  If the stream is also closed, onClose will also be
     * closed.  Does nothing by default.
     */
    public void onError(DSElement details) {
    }

    public OutboundRequest setPath(String path) {
        this.path = path;
        return this;
    }

}
