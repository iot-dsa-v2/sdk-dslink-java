package com.acuity.iot.dsa.dslink.protocol.message;

import org.iot.dsa.dslink.DSIResponder;
import org.iot.dsa.dslink.DSInvalidPathException;
import org.iot.dsa.node.DSIObject;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.DSPath;

/**
 * Maps a request path into a node tree.
 *
 * @author Aaron Hansen
 */
public class RequestPath {

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

    public RequestPath(String path, DSNode root) {
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
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * The info of the target in it's parent.
     */
    public DSInfo getInfo() {
        if (target == null) {
            getTarget();
        }
        if ((info == null) && (root == target)) {
            info = new RootInfo(root);
        }
        return info;
    }

    /**
     * The parent of the target unless the request was for /
     */
    public DSNode getParent() {
        if (parent == null) {
            getTarget();
        }
        return parent;
    }

    /**
     * If the target is a responder, this is path it should use, not the original path of the
     * request.
     */
    public String getPath() {
        return path;
    }

    public synchronized DSIObject getTarget() {
        if (target == null) {
            target = root;
            parent = root.getParent();
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

    public boolean isResponder() {
        return target instanceof DSIResponder;
    }

    private static class RootInfo extends DSInfo {
        RootInfo(DSNode root) {
            super(null, root);
        }
    }

}
