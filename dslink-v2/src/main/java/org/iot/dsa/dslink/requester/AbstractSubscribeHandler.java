package org.iot.dsa.dslink.requester;

/**
 * Convenience implementation of the handler passed to the subscribe method in the requester.
 *
 * @author Daniel Shapiro, Aaron Hansen
 */
public abstract class AbstractSubscribeHandler
        extends AbstractRequestHandler
        implements OutboundSubscribeHandler {

    /**
     * Returns the param passed to onInit.
     */
    public int getQos() {
        return getParams().toElement().toInt();
    }

}
