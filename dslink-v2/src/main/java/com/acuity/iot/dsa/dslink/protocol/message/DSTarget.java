package com.acuity.iot.dsa.dslink.protocol.message;

import org.iot.dsa.dslink.DSIResponder;
import org.iot.dsa.dslink.DSInvalidPathException;
import org.iot.dsa.node.DSIObject;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.DSPath;
import org.iot.dsa.node.action.DSAction;

/**
 * Information about the target of a request.
 *
 * @author Aaron Hansen
 */
public class DSTarget {

    private String[] names;

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSNode node;
    private DSInfo parentInfo;
    private String path;
    private DSNode root;
    private DSIObject target;
    private DSInfo targetInfo;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public DSTarget(String path, DSNode root) {
        if (root == null) {
            throw new NullPointerException("Null root");
        }
        if (path == null) {
            path = "";
        }
        this.path = path;
        this.root = root;
        names = DSPath.decodePath(path);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * The node closest to the target, if not the target itself.
     */
    public DSNode getNode() {
        if (node == null) {
            getTarget();
        }
        return node;
    }

    /**
     * Info about the parent of the target.  This is helpful when the target is an action on
     * a value.
     */
    public DSInfo getParentInfo() {
        return parentInfo;
    }

    /**
     * The path to the target.  If a DSIResponder is encountered, this will be the path from
     * the responder.
     */
    public String getPath() {
        return path;
    }

    public String[] getPathElements() {
        return names;
    }

    /**
     * The endpoint of a request path.
     */
    public synchronized DSIObject getTarget() {
        if (target == null) {
            target = root;
            targetInfo = root.getInfo();
            node = root.getParent();
            if (node != null) {
                parentInfo = node.getInfo();
            }
            int len = names.length;
            if (len == 0) {
                return target;
            }
            for (int i = 0; i < len; i++) {
                advance(i);
                if (target instanceof DSIResponder) {
                    StringBuilder buf = new StringBuilder();
                    while (++i < len) {
                        buf.append('/');
                        buf.append(names[i]);
                    }
                    if (buf.length() == 0) {
                        buf.append('/');
                    }
                    path = buf.toString();
                    break;
                }
            }
        }
        if ((targetInfo != null) && targetInfo.isNode()) {
            node = targetInfo.getNode();
        }
        return target;
    }

    /**
     * The info for the endpoint of a request path.
     */
    public DSInfo getTargetInfo() {
        if (target == null) {
            getTarget();
        }
        if ((targetInfo == null) && (root == target)) {
            targetInfo = new RootInfo(root);
        }
        return targetInfo;
    }

    /**
     * Whether or not the target is an action.
     */
    public boolean isAction() {
        return getTarget() instanceof DSAction;
    }

    /**
     * Whether or not the target is a DSIResponder.  If so, the path will be from the responder.
     */
    public boolean isResponder() {
        return getTarget() instanceof DSIResponder;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Private Methods
    ///////////////////////////////////////////////////////////////////////////

    /* Moves to the next name in the path elements. */
    private void advance(int nameIndex) {
        if (target == null) {
            throw new DSInvalidPathException(path);
        }
        parentInfo = targetInfo;
        if (target instanceof DSNode) {
            node = (DSNode) target;
            targetInfo = node.getInfo(names[nameIndex]);
        } else {
            targetInfo = node.getDynamicAction(targetInfo, names[nameIndex]);
        }
        if (targetInfo == null) {
            //TODO - need to undo this
            throw new DSInvalidPathException(path);
        }
        target = targetInfo.get();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    private static class RootInfo extends DSInfo {

        RootInfo(DSNode root) {
            super(null, root);
        }

    }

}
