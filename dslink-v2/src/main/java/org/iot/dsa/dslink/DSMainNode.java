package org.iot.dsa.dslink;

import org.iot.dsa.node.DSNode;

/**
 * The root DSNode that triggers a link's custom functionality .  Most links will subclass this and
 * override declareDefaults() to bind their application logic.
 *
 * @author Aaron Hansen
 */
public class DSMainNode extends DSNode {

    /**
     * The parent link or null.
     */
    public DSLink getLink() {
        return (DSLink) getParent();
    }

    @Override
    public String getLogName() {
        return getLogName("main");
    }

    /**
     * Override point, returns true by default.
     */
    public boolean isRequester() {
        return true;
    }

    /**
     * Override point, returns true by default.
     */
    public boolean isResponder() {
        return true;
    }

    /**
     * The parent must be a DSLink instance.
     */
    public void validateParent(DSNode node) {
        if (node instanceof DSLink) {
            return;
        }
        throw new IllegalArgumentException("Must be a child of DSLink");
    }

}
