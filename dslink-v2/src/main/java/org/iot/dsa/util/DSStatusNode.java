package org.iot.dsa.util;

import org.iot.dsa.node.DSIStatus;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.DSStatus;
import org.iot.dsa.node.DSString;

/**
 * A node that has status.  The primary value of this is that it merges the status of the
 * current node with ancestral StatusNodes, and notifies descendant StatusNodes of
 * changes.
 * <p>
 * Status is inherited.  If a parent is disabled, then so should be the children.  If a node
 * representing a remote connection is down, then all descendant points should be down too.
 * <p>
 * Subclasses must:<br>
 * <ul>
 * <li>Call updateStatus(DSStatus,String) when the status of this node changes.
 * <li>Override onStatusChanged to respond to changes in the merged status.
 * </ul>
 *
 * @author Aaron Hansen
 */
public abstract class DSStatusNode extends DSNode implements DSIStatus {

    ///////////////////////////////////////////////////////////////////////////
    // Class Fields
    ///////////////////////////////////////////////////////////////////////////

    protected static final String STATUS = "Status";
    protected static final String STATUS_TEXT = "Status Text";

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    private int lastStatus = getDefaultStatus().getBits();
    protected DSInfo status = getInfo(STATUS);
    private DSStatusNode statusParent;
    protected DSInfo statusText = getInfo(STATUS_TEXT);

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public DSStatus getStatus() {
        return (DSStatus) status.get();
    }

    public String getStatusText() {
        return statusText.getElement().toString();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected Methods
    ///////////////////////////////////////////////////////////////////////////
    @Override
    protected void declareDefaults() {
        super.declareDefaults();
        declareDefault(STATUS, getDefaultStatus(), "The status of this node and any inherited")
                .setReadOnly(true).setTransient(true);
        declareDefault(STATUS_TEXT, DSString.EMPTY, "Description for the current status")
                .setReadOnly(true).setTransient(true);
    }

    /**
     * Override point, called when declaring the status, returns DSStatus.ok by default.
     */
    protected DSStatus getDefaultStatus() {
        return DSStatus.ok;
    }

    /**
     * Scans the ancestral tree for an instance of DSEnabledNode.
     *
     * @return Possibly null.
     */
    protected DSStatusNode getStatusParent() {
        if (statusParent == null) {
            statusParent = (DSStatusNode) getAncestor(DSStatusNode.class);
        }
        return statusParent;
    }

    /**
     * Override point.  Return the status bits that originate only from this node.  Do not use
     * the status property.  Always call super unless you really know what you are doing.
     */
    protected int getThisStatus() {
        return 0;
    }

    /**
     * Scans the subtree for DSStatusNode children and calls onStatusChange(this).  Does not scan
     * below StatusNodes, they can do that themselves.
     */
    protected void notifyStatusDescendants() {
        notifyStatusDescendants(this, this);
    }

    /**
     * Detects changes to the merged status and notifies descendants.
     */
    @Override
    protected void onChildChanged(DSInfo child) {
        if (child == status) {
            try {
                onStatusChanged();
            } catch (Exception x) {
                error(getPath(), x);
            }
            notifyStatusDescendants();
        }
        super.onChildChanged(child);
    }

    @Override
    protected void onStarted() {
        statusParent = null;
        super.onStarted();
    }

    /**
     * Override point, called by onChildChanged before children are notified.  Does nothing by
     * default.
     */
    protected void onStatusChanged() {
    }

    /**
     * Override point, called by a StatusNode ancestor  and the default implementation
     * merges with the local status, and notifies descendants.
     */
    protected void onStatusChanged(DSStatusNode parent) {
        updateStatus(parent);
    }

    protected void updateStatus() {
        updateStatus(getStatusParent());
    }

    /**
     * Subclasses must call this when the status of just this node changes.
     *
     * @param text If null, does not change the status text, anything else will.
     */
    protected void updateStatus(String text) {
        updateStatus(getStatusParent());
        if (text != null) {
            if (!DSUtil.equal(text, getStatusText())) {
                put(statusText, DSString.valueOf(text));
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Private Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Scans the subtree for DSStatusNode children and calls onStatusChange(this).  Does not scan
     * below StatusNodes, they can do that themselves.
     */
    private void notifyStatusDescendants(DSNode parent, DSStatusNode notifier) {
        DSInfo info = parent.getFirstNodeInfo();
        while (info != null) {
            if (info.is(DSStatusNode.class)) {
                try {
                    ((DSStatusNode) info.get()).onStatusChanged(notifier);
                } catch (Exception x) {
                    error(info.getPath(null).toString(), x);
                }
            } else {
                notifyStatusDescendants(info.getNode(), notifier);
            }
            info = info.nextNode();
        }
    }

    /**
     * Only notifies children when the merged status changes.
     */
    private void updateStatus(DSStatusNode parent) {
        int current = getThisStatus();
        if (parent != null) {
            current = parent.getStatus().getBits() | current;
        }
        if (lastStatus == current) {
            return;
        }
        lastStatus = current;
        DSStatus newStatus = DSStatus.valueOf(current);
        if (!newStatus.equals(getStatus())) {
            put(status, newStatus);
            notifyStatusDescendants();
        }
    }

}
