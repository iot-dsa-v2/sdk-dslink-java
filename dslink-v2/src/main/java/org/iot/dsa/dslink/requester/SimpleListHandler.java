package org.iot.dsa.dslink.requester;

import java.util.Map;
import java.util.TreeMap;
import org.iot.dsa.node.DSElement;

/**
 * @author Aaron Hansen
 */
public class SimpleListHandler extends AbstractListHandler {

    ///////////////////////////////////////////////////////////////////////////
    // Class Fields
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    private boolean isInitialized = false;
    private Map<String, DSElement> updates;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * The named update, or null.
     */
    public synchronized DSElement getUpdate(String name) {
        if (updates == null) {
            return null;
        }
        return updates.get(name);
    }

    /**
     * Removes the current updates such that calling hasUpdates immediately after this
     * will return false.
     */
    public synchronized Map<String, DSElement> getUpdates() {
        Map<String, DSElement> ret = updates;
        updates = null;
        return ret;
    }

    /**
     * Adds all updates to the given bucket.
     */
    public synchronized void getUpdates(Map<String, DSElement> bucket) {
        bucket.putAll(updates);
    }

    public synchronized boolean hasUpdates() {
        if (updates == null) {
            return false;
        }
        return !updates.isEmpty();
    }

    /**
     * True after onInitialized has been called.
     */
    public boolean isInitialized() {
        return isInitialized;
    }

    @Override
    public synchronized void onClose() {
        super.onClose();
        notifyAll();
    }

    @Override
    public synchronized void onInitialized() {
        isInitialized = true;
        notifyAll();
    }

    @Override
    public synchronized void onRemove(String name) {
        if (updates != null) {
            updates.remove(name);
        }
        notifyAll();
    }

    @Override
    public synchronized void onUpdate(String name, DSElement value) {
        if (updates == null) {
            updates = new TreeMap<>();
        }
        updates.put(name, value);
        notifyAll();
    }

    /**
     * Waits for the initialed state.
     *
     * @param timeout Passed to Object.wait
     * @throws RuntimeException      if there is an error with the invocation.
     * @throws IllegalStateException if there is a timeout, or if there are any errors.
     */
    public synchronized void waitForInitialized(long timeout) {
        long end = System.currentTimeMillis() + timeout;
        while (!isInitialized) {
            try {
                wait(timeout);
            } catch (Exception x) {
                x.printStackTrace();
            }
            if (isError()) {
                throw getError();
            }
            if (System.currentTimeMillis() >= end) {
                break;
            }
            timeout = end - System.currentTimeMillis();
        }
        if (!isInitialized) {
            throw new IllegalStateException("Timed out");
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected Methods
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Package / Private Methods
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Initialization
    ///////////////////////////////////////////////////////////////////////////

}
