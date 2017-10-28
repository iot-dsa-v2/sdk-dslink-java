package org.iot.dsa.dslink.requester;

import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;

/**
 * Parameter to invoke method on DSIRequester.  Provides details about the invocation as well as
 * callbacks for various state changes.
 *
 * @author Daniel Shapiro, Aaron Hansen
 */
public class AbstractInvokeHandler
        extends AbstractRequestHandler
        implements OutboundInvokeHandler {

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSMap params;
    private String path;
    private OutboundRequestStub stub;

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Returns the value passed to onInit.
     */
    public DSMap getParams() {
        return params;
    }

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
     * Does nothing.
     *
     * <p>
     *
     * {@inheritDoc}
     */
    @Override
    public void onColumns(DSList list) {
    }

    /**
     * Sets the fields so they can be access via the corresponding getters.
     *
     * <p>
     *
     * {@inheritDoc}
     */
    @Override
    public void onInit(String path, DSMap params, OutboundRequestStub stub) {
        this.path = path;
        this.params = params;
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
    public void onInsert(int index, DSList rows) {
    }

    /**
     * Does nothing.
     *
     * <p>
     *
     * {@inheritDoc}
     */
    @Override
    public void onMode(Mode mode) {
    }

    /**
     * Does nothing.
     *
     * <p>
     *
     * {@inheritDoc}
     */
    @Override
    public void onReplace(int start, int end, DSList rows) {
    }

    /**
     * Does nothing.
     *
     * <p>
     *
     * {@inheritDoc}
     */
    @Override
    public void onTableMeta(DSMap map) {
    }

    /**
     * Does nothing.
     *
     * <p>
     *
     * {@inheritDoc}
     */
    @Override
    public void onUpdate(DSList row) {
    }

}
