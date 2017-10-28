package org.iot.dsa.dslink.requester;

import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;

/**
 * Convenience implementation of the callback passed to the invoke method in the requester.
 *
 * @author Daniel Shapiro, Aaron Hansen
 */
public abstract class AbstractInvokeHandler implements OutboundInvokeHandler {

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSMap params;
    private String path;
    private OutboundStream stream;

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
    public OutboundStream getStub() {
        return stream;
    }

    /**
     * Does nothing.
     *
     * <p>
     *
     * {@inheritDoc}
    @Override
    public void onColumns(DSList list) {
    }
     */

    /**
     * Sets the fields so they can be access via the corresponding getters.
     *
     * <p>
     *
     * {@inheritDoc}
     */
    @Override
    public void onInit(String path, DSMap params, OutboundStream stream) {
        this.path = path;
        this.params = params;
        this.stream = stream;
    }

    /**
     * Does nothing.
     *
     * <p>
     *
     * {@inheritDoc}
    @Override
    public void onInsert(int index, DSList rows) {
    }
     */

    /**
     * Does nothing.
     *
     * <p>
     *
     * {@inheritDoc}
    @Override
    public void onMode(Mode mode) {
    }
     */

    /**
     * Does nothing.
     *
     * <p>
     *
     * {@inheritDoc}
    @Override
    public void onReplace(int start, int end, DSList rows) {
    }
     */

    /**
     * Does nothing.
     *
     * <p>
     *
     * {@inheritDoc}
    @Override
    public void onTableMeta(DSMap map) {
    }
     */

    /**
     * Does nothing.
     *
     * <p>
     *
     * {@inheritDoc}
    @Override
    public void onUpdate(DSList row) {
    }
     */

}
