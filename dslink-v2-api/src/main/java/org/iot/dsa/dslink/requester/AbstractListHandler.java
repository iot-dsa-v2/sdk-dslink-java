package org.iot.dsa.dslink.requester;

/**
 * Convenience implementation of the handler passed to the invoke method in the requester.
 * <p>
 * onUpdate will be called until the initial state is fully loaded.  After which onInitialized will
 * be called.  After that, onUpdate and onRemove will be called for state changes.
 *
 * @author Daniel Shapiro, Aaron Hansen
 */
public abstract class AbstractListHandler
        extends AbstractRequestHandler
        implements OutboundListHandler {

}

