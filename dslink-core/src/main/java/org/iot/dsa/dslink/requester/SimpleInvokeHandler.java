package org.iot.dsa.dslink.requester;

import com.acuity.iot.dsa.dslink.DSProtocolException;
import org.iot.dsa.dslink.DSRequestException;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;

/**
 * Action handler for non-tables/streams.
 * <p>
 * Call getResult(long timeout) to block until the invocation is complete. It will either return
 * the result (possibly null), or throw an exception.
 *
 * @author Aaron Hansen
 */
public class SimpleInvokeHandler extends AbstractInvokeHandler {

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private boolean autoClose = true;
    private boolean closed = false;
    private RuntimeException error;
    private DSList result;

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Waits for the stream to close before returning, or the timeout to occur.
     *
     * @param timeout Passed to Object.wait
     * @return Null, or the first update.
     * @throws RuntimeException if there is a timeout, or if there are any errors.
     */
    public DSList getResult(long timeout) {
        synchronized (this) {
            if (!closed) {
                try {
                    wait(timeout);
                } catch (Exception x) {
                }
            }
        }
        if (error != null) {
            throw error;
        }
        if (!closed) {
            throw new IllegalStateException("Action timed out");
        }
        return result;
    }

    /**
     * True by default, whether or not to close the stream upon receiving the first result.
     */
    public boolean isAutoClose() {
        return autoClose;
    }

    /**
     * Causes getResult to return.
     */
    public void onClose() {
        synchronized (this) {
            closed = true;
            notifyAll();
        }
    }

    /**
     * Will create an exception to be thrown by getResult.
     */
    public void onError(ErrorType type, String msg) {
        synchronized (this) {
            error = ErrorType.makeException(type, msg);
            getStream().closeStream();
            notifyAll();
        }
    }

    /**
     * Does nothing.
     */
    public void onColumns(DSList list) {
    }

    /**
     * Will result in an error since tables and streams are not supported.
     */
    public void onInsert(int index, DSList rows) {
        synchronized (this) {
            error = new DSRequestException("Tables and streams not supported");
            getStream().closeStream();
            notifyAll();
        }
    }

    /**
     * Does nothing.
     */
    public void onMode(Mode mode) {
    }

    /**
     * Will result in an error since tables and streams are not supported.
     */
    public void onReplace(int start, int end, DSList rows) {
        synchronized (this) {
            error = new DSRequestException("Tables and streams not supported");
            getStream().closeStream();
            notifyAll();
        }
    }

    public void onTableMeta(DSMap map) {
    }

    /**
     * Captures the result and if auto-close is true, closes the stream.
     */
    public void onUpdate(DSList row) {
        synchronized (this) {
            result = row;
            if (autoClose) {
                getStream().closeStream();
            }
        }
    }

    /**
     * Whether or not to auto close the stream on the first update.  True by default, this
     * only needs to be called to disable.
     */
    public SimpleInvokeHandler setAutoClose(boolean arg) {
        autoClose = arg;
        return this;
    }

}
