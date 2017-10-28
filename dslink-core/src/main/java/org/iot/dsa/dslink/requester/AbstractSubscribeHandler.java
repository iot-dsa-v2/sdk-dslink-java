package org.iot.dsa.dslink.requester;

import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSStatus;
import org.iot.dsa.time.DSDateTime;

/**
 * Parameter to the subscribe method on DSIRequester.  Provides details about the subscription as
 * well as callbacks for various state changes.
 *
 * @author Daniel Shapiro, Aaron Hansen
 */
public class AbstractSubscribeHandler
        extends AbstractRequestHandler
        implements OutboundSubscribeHandler {

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private String path;
    private int qos;
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
    public int getQos() {
        return qos;
    }

    /**
     * Returns the value passed to onInit.
     */
    public OutboundRequestStub getStub() {
        return stub;
    }

    /**
     * Sets the fields so they can be accessed via the corresponding getters.
     *
     * <p>
     *
     * {@inheritDoc}
     */
    @Override
    public void onInit(String path, int qos, OutboundRequestStub stub) {
        this.path = path;
        this.qos = qos;
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
    public void onUpdate(DSDateTime dateTime, DSElement value, DSStatus status) {
    }

}
