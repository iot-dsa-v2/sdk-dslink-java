package org.iot.dsa.dslink.responder;

import org.iot.dsa.dslink.Action;
import org.iot.dsa.dslink.DSIResponder;
import org.iot.dsa.dslink.Node;
import org.iot.dsa.dslink.Value;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSIValue;

/**
 * How to respond to a list request.  Implementations must first send the target of the
 * request.  Then any children, listComplete and finally changes as they happen.  If the target is
 * resent, that will clear all prior state so children should be also resent.
 *
 * @author Aaron Hansen
 * @see DSIResponder#onList(InboundListRequest)
 */
public interface InboundListRequest extends InboundRequest {

    /**
     * Allows the responder to forcefully close the list stream.
     */
    public void close();

    /**
     * Allows the responder to forcefully close the list stream with an error.
     */
    public void close(Exception reason);

    /**
     * Whether or not the list stream is still open.
     */
    public boolean isOpen();

    /**
     * Call after the initial state of the target and it's children has been sent.
     */
    public void listComplete();

    /**
     * Add or change any metadata on the target of the request after beginUpdates.  Can also be
     * used as a pass-thru mechanism.
     */
    public void send(String name, DSElement value);

    /**
     * Add or update a child action to the list.
     *
     * @param name        Will be encoded, it's not usually necessary to have a display name.
     * @param displayName Can be null.
     * @param admin       Whether or not the action requires admin level permission.
     * @param readonly    Whether or not the action requires write permission.
     */
    public void sendAction(String name, String displayName, boolean admin, boolean readonly);

    /**
     * Add or update a child node to the list.
     *
     * @param name        Will be encoded, it's not usually necessary to have a display name.
     * @param displayName Can be null.
     * @param admin       Whether or not admin level required to see node.
     */
    public void sendNode(String name, String displayName, boolean admin);

    /**
     * The responder should call this whenever a child or metadata is removed.
     */
    public void sendRemove(String name);

    /**
     * This should be called first to provide details about the target and should be an
     * action, node or value.  Subsequent calls will reset the state of the list such that
     * children will need to be resent as well as sendBeginUpdates.
     *
     * @param object Cannot be the generic ApiObject interface, must be one of the subtypes; action,
     *               node or value.
     * @see Action
     * @see Node
     * @see Value
     */
    public void sendTarget(Node object);

    /**
     * Add or update a child value to the list.
     *
     * @param name        Will be encoded, it's not usually necessary to have a display name.
     * @param displayName Can be null.
     * @param type        Used for encoding the type only.
     * @param admin       Whether or not admin level required to see node.
     * @param readonly    Whether or not the value is writable.
     */
    public void sendValue(String name,
                          String displayName,
                          DSIValue type,
                          boolean admin,
                          boolean readonly);

}
