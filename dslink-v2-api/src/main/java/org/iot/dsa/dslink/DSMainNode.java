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
     * The ancestral DSLink, or null.
     */
    public DSLink getLink() {
        return getAncestor(DSLink.class);
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

}
