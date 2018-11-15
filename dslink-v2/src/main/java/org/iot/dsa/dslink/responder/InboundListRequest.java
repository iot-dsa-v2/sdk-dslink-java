package org.iot.dsa.dslink.responder;

import org.iot.dsa.dslink.DSIResponder;

/**
 * The details about an incoming list request passed to the responder.
 *
 * @author Aaron Hansen
 * @see DSIResponder#onList(InboundListRequest)
 */
public interface InboundListRequest extends InboundRequest {

    /**
     * The responder should call this whenever a child is added.
     */
    public void childAdded(ApiObject child);

    /**
     * The responder should call this whenever a child is removed.
     */
    public void childRemoved(String name);

    /**  TODO
     * The response should call this whenever the meta-data about the target changes.
     public void metadataChanged();
     */

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

}
