package org.iot.dsa.dslink.responder;

import org.iot.dsa.dslink.DSIResponder;
import org.iot.dsa.node.DSElement;

/**
 * The mechanism by which a responder and provide updates to a list request.
 *
 * @author Aaron Hansen
 * @see DSIResponder#onList(InboundListRequest)
 */
public interface InboundListRequest extends InboundRequest {

    /**
     * The responder should call this whenever a child changes or is added.
     */
    public void added(String name, ApiObject child);

    /**
     * The responder should call this whenever metadata changes.
     */
    public void changed(String name, DSElement value);

    /**
     * Allows the responder to forcefully close the list stream.
     */
    public void close();

    /**
     * Allows the responder to forcefully close the list stream.
     */
    public void close(Exception reason);

    /**
     * Whether or not the list stream is still open.
     */
    public boolean isOpen();

    /**
     * The responder should call this whenever a child is removed.
     */
    public void removed(String name);

}
