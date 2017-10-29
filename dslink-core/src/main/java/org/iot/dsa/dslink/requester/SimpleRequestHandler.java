package org.iot.dsa.dslink.requester;

/**
 * Empty callback implementations.
 *
 * @author Daniel Shapiro, Aaron Hansen
 */
public class SimpleRequestHandler implements OutboundRequestHandler {

    /**
     * An instance that can be used for those requests where the callbacks don't really matter.
     */
    public static final SimpleRequestHandler DEFAULT = new SimpleRequestHandler();

    /**
     * Does nothing by default.
     *
     * <p>
     *
     * {@inheritDoc}
     */
    @Override
    public void onClose() {
    }

    /**
     * Does nothing by default.
     *
     * <p>
     *
     * {@inheritDoc}
     * @param type
     * @param msg
     * @param detail
     */
    @Override
    public void onError(String type, String msg, String detail) {
    }

}
