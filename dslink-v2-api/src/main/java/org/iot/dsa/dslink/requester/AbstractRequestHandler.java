package org.iot.dsa.dslink.requester;

import org.iot.dsa.node.DSIValue;

/**
 * Convenience base type for all callbacks passed to requester method invocations.
 *
 * @author Daniel Shapiro, Aaron Hansen
 */
public abstract class AbstractRequestHandler implements OutboundRequestHandler {

    ///////////////////////////////////////////////////////////////////////////
    // Class Fields
    ///////////////////////////////////////////////////////////////////////////

    private boolean closed = false;
    private RuntimeException error;
    private DSIValue params;
    private String path;
    private OutboundStream stream;

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Error msg passed to onError
     */
    public RuntimeException getError() {
        return error;
    }

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
     * True if onClose ws callsed.
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * True if onError was called.
     */
    public boolean isError() {
        return error != null;
    }

    @Override
    public synchronized void onClose() {
        closed = true;
        notifyAll();
    }

    @Override
    public synchronized void onError(ErrorType type, String msg) {
        error = ErrorType.makeException(type, msg);
        notifyAll();
    }

    @Override
    public void onInit(String path, DSIValue params, OutboundStream stream) {
        this.path = path;
        this.params = params;
        this.stream = stream;
    }

    /**
     * Waits for any callback from the responder.  Will return immediately if already closed.
     * Subclasses should synchronize responder callback methods and notifyAll() at the end
     * of the method.
     *
     * @param timeout Passed to Object.wait
     * @throws RuntimeException      if there is an error with the invocation.
     * @throws IllegalStateException if there is a timeout, or if there are any errors.
     */
    public void waitForCallback(long timeout) {
        synchronized (this) {
            if (!closed) {
                long end = System.currentTimeMillis() + timeout;
                try {
                    wait(timeout);
                } catch (Exception x) {
                }
                if (isError()) {
                    throw getError();
                }
                if (System.currentTimeMillis() > end) {
                    throw new IllegalStateException("Timed out");
                }
            }
        }
    }

    /**
     * Waits for the stream to close or the timeout to occur.
     *
     * @param timeout Passed to Object.wait
     * @throws IllegalStateException if there is a timeout, or if there are any errors.
     */
    public void waitForClose(long timeout) {
        long end = System.currentTimeMillis() + timeout;
        synchronized (this) {
            while (!closed) {
                try {
                    wait(timeout);
                } catch (Exception x) {
                }
                if (isError()) {
                    throw getError();
                }
                if (!closed && (System.currentTimeMillis() > end)) {
                    throw new IllegalStateException("Timed out");
                }
            }
        }
    }


}
