package org.iot.dsa.dslink.requester;

import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSStatus;
import org.iot.dsa.time.DSDateTime;

/**
 * @author Aaron Hansen
 */
public class SimpleSubscribeHandler extends AbstractSubscribeHandler {

    ///////////////////////////////////////////////////////////////////////////
    // Class Fields
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    private Update head;
    private Update tail;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    public synchronized boolean hasUpdates() {
        return head != null;
    }

    /**
     * Returns the next update, blocking for the given timeout.
     *
     * @param timeout 0 or less means do not block.
     * @return Possibly null if no timeout.
     * @throws RuntimeException If the timeout expires or the stream is closed.
     */
    public synchronized Update nextUpdate(long timeout) {
        if ((timeout > 0) && (head == null)) {
            long now = System.currentTimeMillis();
            long end = now + timeout;
            while (head == null) {
                try {
                    wait(timeout);
                } catch (InterruptedException ignore) {
                }
                if (head != null) {
                    break;
                }
                if (isError()) {
                    throw getError();
                }
                now = System.currentTimeMillis();
                if (isClosed() || (now > end)) {
                    throw new IllegalStateException("Timed out");
                }
                timeout = end - now;
            }
        }
        Update ret = head;
        if (ret == null) {
            return null;
        }
        head = head.next;
        if (head == null) {
            tail = null;
        }
        return ret;
    }

    @Override
    public synchronized void onUpdate(DSDateTime ts, DSElement val, DSStatus sts) {
        Update update = new Update();
        update.timestamp = ts;
        update.value = val;
        update.status = sts;
        if (head == null) {
            head = update;
            tail = update;
        } else {
            tail.next = update;
            tail = update;
        }
        notifyAll();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected Methods
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Package Methods
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Private Methods
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    public static class Update {

        Update next;
        public DSStatus status;
        public DSDateTime timestamp;
        public DSElement value;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Initialization
    ///////////////////////////////////////////////////////////////////////////

}
