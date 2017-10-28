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
public class AbstractListHandler
        extends AbstractRequestHandler
        implements OutboundListHandler {

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private String path;
    private OutboundRequestStub stub;

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Returns the value passed to onInit.
     */
    public String getPath() {
        return path;
    }

    /**
     * Returns the value passed to onInit.
     */
    public OutboundRequestStub getStub() {
        return stub;
    }

    /**
     * Sets the fields so they can be access via the corresponding getters.
     *
     * <p>
     *
     * {@inheritDoc}
     */
    @Override
    public void onInit(String path, OutboundRequestStub stub) {
        this.path = path;
        this.stub = stub;
    }

    /**
     * Does nothing.
     *
     * <p>
     *
     * {@inheritDoc}
     */
    @Override
    public void onInitialized() {
    }

    /**
     * Does nothing.
     *
     * <p>
     *
     * {@inheritDoc}
     */
    @Override
    public void onRemove(String name) {
    }

    /**
     * Does nothing.
     *
     * <p>
     *
     * {@inheritDoc}
     */
    @Override
    public void onUpdate(String name, DSElement value) {
    }

}

