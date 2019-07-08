package org.iot.dsa.dslink.requester;

import org.iot.dsa.node.DSIValue;

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
    private DSIValue params;
    private String path;
    private OutboundStream stream;

    @Override
    public DSIValue getParameters() {
        return params;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public OutboundStream getStream() {
        return stream;
    }

    /**
     * Does nothing by default.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void onClose() {
    }

    /**
     * Does nothing by default.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void onError(ErrorType type, String msg) {
    }

    @Override
    public void onInit(String path, DSIValue params, OutboundStream stream) {
        this.path = path;
        this.params = params;
        this.stream = stream;
    }

}
