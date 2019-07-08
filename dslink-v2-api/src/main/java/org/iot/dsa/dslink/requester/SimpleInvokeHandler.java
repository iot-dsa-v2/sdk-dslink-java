package org.iot.dsa.dslink.requester;

import java.util.ArrayList;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;

/**
 * Action handler for non-tables/streams.
 * <p>
 * Call getUpdate(long timeout) to block until the invocation is complete. It will either return
 * the result (possibly null), or throw an exception.
 *
 * @author Aaron Hansen
 */
public class SimpleInvokeHandler extends AbstractInvokeHandler {

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    private boolean closed = false;
    private DSList columns;
    private RuntimeException error;
    private Mode mode;
    private DSMap tableMeta;
    private ArrayList<DSList> updates;

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * It is possible for a values action to not return columns.
     *
     * @return Beware of null.
     */
    public int getColumnCount() {
        if (columns == null) {
            return 0;
        }
        return columns.size();
    }

    /**
     * The metadata map for the given column.
     */
    public DSMap getColumnMetadata(int idx) {
        return columns.getMap(idx);
    }

    public Mode getMode() {
        return mode;
    }

    public DSMap getTableMeta() {
        return tableMeta;
    }

    /**
     * The next available update, or null for actions return void.
     * Will wait for an update if one isn't available.  Will return all updates before
     * throwing any exceptions.
     *
     * @param timeout How long to wait for an update or the stream to close.
     * @return Null if the action doesn't return anything.
     * @throws RuntimeException if there is a timeout, or if there are any errors.
     */
    public DSList getUpdate(long timeout) {
        long end = System.currentTimeMillis() + timeout;
        synchronized (this) {
            while (!isClosed() && !hasUpdates()) {
                try {
                    wait(timeout);
                } catch (Exception expected) {
                }
                if (System.currentTimeMillis() > end) {
                    break;
                }
                timeout = end - System.currentTimeMillis();
            }
            if (isError()) {
                throw error;
            }
            if (hasUpdates()) {
                return updates.remove(0);
            }
            if (isClosed()) {
                return null;
            }
            if (System.currentTimeMillis() > end) {
                throw new IllegalStateException("Action timed out");
            }
            return null;
        }
    }

    /**
     * Takes the updates such that subsequent calls will never return the same updates, except when
     * there are no updates in which case this returns null.
     *
     * @return Possibly null.
     */
    public synchronized ArrayList<DSList> getUpdates() {
        ArrayList<DSList> ret = updates;
        updates = null;
        return ret;
    }

    public synchronized boolean hasUpdates() {
        if (updates == null) {
            return false;
        }
        return !updates.isEmpty();
    }

    @Override
    public synchronized void onColumns(DSList list) {
        this.columns = list;
        notifyAll();
    }

    /**
     * Does nothing other than notify and threads waiting on this instance.
     */
    @Override
    public synchronized void onInsert(int index, DSList rows) {
        notifyAll();
    }

    @Override
    public synchronized void onMode(Mode mode) {
        this.mode = mode;
        notifyAll();
    }

    /**
     * Does nothing other than notify and threads waiting on this instance.
     */
    @Override
    public synchronized void onReplace(int start, int end, DSList rows) {
        notifyAll();
    }

    @Override
    public synchronized void onTableMeta(DSMap map) {
        this.tableMeta = map;
        notifyAll();
    }

    /**
     * Captures the result and if auto-close is true, closes the stream.
     */
    @Override
    public synchronized void onUpdate(DSList row) {
        if (updates == null) {
            updates = new ArrayList<>();
        }
        updates.add(row);
        notifyAll();
    }

}
