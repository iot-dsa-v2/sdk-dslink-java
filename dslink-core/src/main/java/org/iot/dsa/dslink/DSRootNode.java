package org.iot.dsa.dslink;

import java.util.logging.Logger;
import org.iot.dsa.node.DSNode;

/**
 * The root DSNode that triggers a link's custom functionality .  Most links will subclass this and
 * override declareDefaults() to bind their application logic.
 *
 * @author Aaron Hansen
 */
public class DSRootNode extends DSNode {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Creates a child logger of the link.
     */
    protected Logger getLogger(String name) {
        if (name.startsWith(".")) {
            return Logger.getLogger(getLink().getLinkName() + name);
        }
        return Logger.getLogger(getLink().getLinkName() + '.' + name);
    }

    /**
     * The parent link or null.
     */
    public DSLink getLink() {
        return (DSLink) getParent();
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

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

}
