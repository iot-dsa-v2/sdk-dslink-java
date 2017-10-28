package org.iot.dsa.dslink.requester;

import org.iot.dsa.node.DSElement;

/**
 * Convenience implementation of the callback passed to the invoke method in the requester.
 *
 * <p>
 *
 * onUpdate will be called until the initial state is fully loaded.  After which onInitialized will
 * be called.  After that, onUpdate and onRemove will be called for state changes.
 *
 * <p>
 *
 * Configs, or node metadata names start with $.  Attributes start with @.  Anything else represents
 * a child.  Child maps will only contain configs/node metadata.
 *
 * @author Daniel Shapiro, Aaron Hansen
 */
public abstract class AbstractListHandler implements OutboundListHandler {

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private String path;
    private OutboundStream stream;

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
    public OutboundStream getStub() {
        return stream;
    }

    /**
     * Sets the fields so they can be accessed with the corresponding getters.
     *
     * <p>
     *
     * {@inheritDoc}
     */
    @Override
    public void onInit(String path, OutboundStream stream) {
        this.path = path;
        this.stream = stream;
    }

    /**
     * Does nothing.
     *
     * <p>
     *
     * {@inheritDoc}
    @Override
    public void onInitialized() {
    }
     */

    /**
     * Does nothing.
     *
     * <p>
     *
     * {@inheritDoc}
    @Override
    public void onRemove(String name) {
    }
     */

    /**
     * Does nothing.
     *
     * <p>
     *
     * {@inheritDoc}
    @Override
    public void onUpdate(String name, DSElement value) {
    }
     */

}

