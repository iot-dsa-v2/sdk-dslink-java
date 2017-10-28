package org.iot.dsa.dslink.requester;

import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSStatus;
import org.iot.dsa.time.DSDateTime;

/**
 * Convenience implementation of the callback passed to the subscribe method in the requester.
 *
 * @author Daniel Shapiro, Aaron Hansen
 */
public abstract class AbstractSubscribeHandler implements OutboundSubscribeHandler {

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private String path;
    private int qos;
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
    public int getQos() {
        return qos;
    }

    /**
     * Returns the value passed to onInit.
     */
    public OutboundStream getStub() {
        return stream;
    }

    /**
     * Sets the fields so they can be accessed via the corresponding getters.
     *
     * <p>
     *
     * {@inheritDoc}
     */
    @Override
    public void onInit(String path, int qos, OutboundStream stream) {
        this.path = path;
        this.qos = qos;
        this.stream = stream;
    }

    /**
     * Does nothing.
     *
     * <p>
     *
     * {@inheritDoc}
    @Override
    public void onUpdate(DSDateTime dateTime, DSElement value, DSStatus status) {
    }
     */

}
