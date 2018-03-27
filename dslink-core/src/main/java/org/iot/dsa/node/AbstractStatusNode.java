package org.iot.dsa.node;

import com.acuity.iot.dsa.dslink.transport.DSTransport;

/**
 * A convenience implementation of a node that has status in a tree of similar nodes.
 * <p>
 * The following status can be inherited by children: disabled, down and fault.
 * <p>
 * This provides override hooks for finding the parent and children.
 *
 * @author Aaron Hansen
 */
public abstract class AbstractStatusNode extends DSNode implements DSIStatus {

    public static final DSString PARENT_DISABLED = DSString.valueOf("Parent disabled");
    public static final String STATUS = "Status";
    public static final String STATUS_TEXT = "Status-Text";

    private int lastBits = 0;
    private DSInfo status = getInfo(STATUS);
    private DSInfo statusText = getInfo(STATUS_TEXT);

    @Override
    public void declareDefaults() {
        declareDefault(STATUS, DSStatus.ok).setReadOnly(true).setTransient(true);
        declareDefault(STATUS_TEXT, DSString.EMPTY).setReadOnly(true).setTransient(true);
    }

    /**
     * Override point, this should evaluate configuration and if there is a fault
     * condition, return a string description of it, otherwise null.
     *
     * @return Null if there is no fault originating with this node.
     */
    protected DSString getFaultText() {
        return null;
    }

    /**
     * Override point.  If the parent implements DSStatus, this returns its status,
     * otherwise DSStatus.ok.
     */
    protected DSStatus getParentStatus() {
        DSNode parent = getParent();
        if (parent instanceof AbstractStatusNode) {
            return ((AbstractStatusNode) parent).toStatus();
        }
        return DSStatus.ok;
    }

    public DSString getStatusText() {
        return (DSString) statusText.getObject();
    }

    /**
     * Override point.  This just returns true.
     */
    public boolean isDown() {
        return true;
    }

    /**
     * Override point.  This just returns true.
     */
    public boolean isEnabled() {
        return true;
    }

    /**
     * False if the status is disabled, down or fault.  This can be used to disable
     * functionality such as polling.
     */
    public boolean isOperational() {
        DSStatus s = toStatus();
        if (s.isDisabled()) {
            return false;
        }
        if (s.isDown()) {
            return false;
        }
        if (s.isFault()) {
            return false;
        }
        return true;
    }

    @Override
    public DSStatus toStatus() {
        return (DSStatus) status.getObject();
    }

    /**
     * Call this when the status state should be refreshed.
     */
    protected void update() {
        update(getParentStatus());
    }

    void update(DSStatus parentStatus) {
        DSStatus status = getParentStatus();
        if (status.isDisabled()) {
            put(this.status, DSStatus.disabled);
            put(this.statusText, PARENT_DISABLED);
            return;
        }
        int bits = 0;
        DSString stsTxt = null;
        if (!isEnabled()) {
            bits = DSStatus.DISABLED;
            stsTxt = DSString.EMPTY;
        }
        if (isDown()) {
            bits |= DSStatus.DOWN;
        }
        try {
            DSString faultText = getFaultText();
            if (faultText != null) {
                bits |= DSStatus.FAULT;
                if (stsTxt != null) {
                    stsTxt = faultText;
                }
            }
        } catch (Exception x) {
            error(getPath(), x);
            if (stsTxt == null) {
                stsTxt = DSString.valueOf(x.toString());
            }
        }
        bits |= status.getBits();
        DSStatus myStatus = toStatus();
        int origBits = myStatus.getBits();
        if (bits == origBits) {
            return;
        }
        status = DSStatus.valueOf(bits);
        if (!status.equals(myStatus)) {
            put(this.status, myStatus);
        }
        if (stsTxt == null) {
            if (getStatusText().equals(DSString.EMPTY)) {
                put(statusText, stsTxt);
            }
        } else if (!getStatusText().equals(stsTxt)){
            put(statusText, stsTxt);
        }
        DSInfo info = getFirstInfo();
        while (info != null) {
            if (info.getObject() instanceof AbstractStatusNode) {
                ((AbstractStatusNode)info.getObject()).update(status);
            }
        }
    }

}
