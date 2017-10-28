package org.iot.dsa.dslink.requester;

import org.iot.dsa.node.DSElement;

/**
 * Callback mechanism passed to the list method on DSIRequester.
 *
 * <ul>
 *
 * <li>onInit will be called first, before the list method returns.
 *
 * <li>onUpdate will be called until the initial state is fully loaded.
 *
 * <li>onInitialized when the initial state of the target has been fully loaded.
 *
 * <li>onRemove and onUpdate will be called for subsequent changed to the target.
 *
 * </ul>
 *
 * Configs, or node metadata names, start with $.  Attributes start with @.  Anything else
 * represents a child.  Child maps will only contain configs/node metadata.
 *
 * @author Daniel Shapiro, Aaron Hansen
 */
public interface OutboundListHandler extends OutboundRequestHandler {

    /**
     * Called by the requester before returning from the list method.
     *
     * @param path Parameter to the list method.
     * @param stream Mechanism to close the request stream.
     */
    public void onInit(String path, OutboundStream stream);

    /**
     * Called once the initial state of the target has been transmitted.  This is a good place to
     * call close if not interested in future updates.
     */
    public void onInitialized();

    /**
     * Only called after onOpen(), indicates something about the target of the request has been
     * removed.
     *
     * @param name Name of the the thing that has been removed.
     */
    public void onRemove(String name);

    /**
     * Called to provide a value for node metadata, attribute or child.  After onOpen is called
     * these represent changes of state.
     *
     * @param name  Node metadata starts with $, attributes @, otherwise represents a child.
     * @param value If a child, will be a map.
     */
    public void onUpdate(String name, DSElement value);

}

