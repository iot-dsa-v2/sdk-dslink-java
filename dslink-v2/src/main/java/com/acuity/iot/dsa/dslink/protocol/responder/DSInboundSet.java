package com.acuity.iot.dsa.dslink.protocol.responder;

import com.acuity.iot.dsa.dslink.protocol.message.DSTarget;
import org.iot.dsa.dslink.DSIResponder;
import org.iot.dsa.dslink.DSPermissionException;
import org.iot.dsa.dslink.DSRequestException;
import org.iot.dsa.dslink.responder.InboundSetRequest;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSIObject;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.security.DSPermission;

public class DSInboundSet extends DSInboundRequest implements InboundSetRequest, Runnable {

    private DSPermission permission;
    private DSElement value;

    public DSInboundSet(DSElement value, DSPermission permission) {
        this.permission = permission;
        this.value = value;
    }

    @Override
    public DSPermission getPermission() {
        return permission;
    }

    @Override
    public DSElement getValue() {
        return value;
    }

    @Override
    public void run() {
        try {
            DSTarget path = new DSTarget(getPath(), getLink().getRootNode());
            if (path.isResponder()) {
                DSIResponder responder = (DSIResponder) path.getTarget();
                setPath(path.getPath());
                responder.onSet(this);
            } else {
                DSInfo info = path.getTargetInfo();
                if ((info != null) && info.isReadOnly()) {
                    throw new DSRequestException("Not writable: " + getPath());
                }
                if (!permission.isConfig()) {
                    if ((info != null) && info.isAdmin()) {
                        throw new DSPermissionException("Config permission required");
                    } else if (DSPermission.WRITE.isGreaterThan(permission)) {
                        throw new DSPermissionException("Write permission required");
                    }
                }
                DSIObject obj = path.getTarget();
                if (obj instanceof DSNode) {
                    ((DSNode) obj).onSet(value);
                } else {
                    //since not a node, there must be a parent
                    DSNode parent = info.getParent();
                    DSIValue current = info.getValue();
                    if (current == null) {
                        if (info.isValue()) {
                            current = info.getValue();
                        }
                    }
                    if (current != null) {
                        current = current.valueOf(value);
                    } else {
                        current = value;
                    }
                    parent.onSet(info, current);
                }
            }
            sendClose();
        } catch (Exception x) {
            error(getPath(), x);
            getResponder().sendError(this, x);
        }
    }

    /**
     * Override point for V2.
     */
    protected void sendClose() {
        getResponder().sendClose(getRequestId());
    }

}
