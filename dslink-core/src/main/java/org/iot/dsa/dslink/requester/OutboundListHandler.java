package org.iot.dsa.dslink.requester;

import org.iot.dsa.node.DSElement;

/**
 * Callback mechanism passed to the list method on DSIRequester.
 *
 * <p>
 *
 * onUpdate will be called until the initial state is fully loaded.  After which onOpen will be
 * called.  After that, onUpdate and onRemove will be called for state changes.
 *
 * <p>
 *
 * Configs, or node metadata names start with $.  Attributes start with @.  Anything else represents
 * a child.  Child maps will only contain configs/node metadata.
 *
 * @author Daniel Shapiro, Aaron Hansen
 */
public interface OutboundListHandler extends OutboundRequestHandler {

    /**
     * Called by the requester before returning from the list method.
     *
     * @param path   Parameter to the list method.
     * @param stub Mechanism to close the request stream.
     */
    public void onInit(String path, OutboundRequestStub stub);

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

