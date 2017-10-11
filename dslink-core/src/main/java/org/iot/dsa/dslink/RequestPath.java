package org.iot.dsa.dslink;

import org.iot.dsa.node.DSIObject;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.DSPath;

/**
 * Maps a request path into a node tree.
 *
 * @author Aaron Hansen
 */
class RequestPath {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSIObject target;
    private DSInfo info;
    private String[] names;
    private DSNode parent;
    private String path;
    private DSNode root;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    RequestPath(String path, DSNode root) {
        this.path = path;
        this.root = root;
        names = DSPath.decodePath(path);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * The info of the target in the parent.
     */
    DSInfo getInfo() {
        if (target == null) {
            getTarget();
        }
        return info;
    }

    /**
     * The parent of the target unless the request was for /
     */
    DSNode getParent() {
        if (parent == null) {
            getTarget();
        }
        return parent;
    }

    /**
     * If the target is a responder, this is path to send to it.
     */
    String getPath() {
        return path;
    }

    DSIObject getTarget() {
        if (parent == null) {
            parent = root.getParent();
            target = root;
            info = root.getInfo();
            int len = names.length;
            if (len == 0) {
                return target;
            }
            for (int i = 0; i < len; i++) {
                if (target == null) {
                    throw new DSInvalidPathException(path);
                }
                if (!(target instanceof DSNode)) {
                    throw new DSInvalidPathException(path);
                }
                parent = (DSNode) target;
                info = parent.getInfo(names[i]);
                if (info == null) {
                    throw new DSInvalidPathException(path);
                }
                target = info.getObject();
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
        return target;
    }

    boolean isResponder() {
        return target instanceof DSIResponder;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

}
