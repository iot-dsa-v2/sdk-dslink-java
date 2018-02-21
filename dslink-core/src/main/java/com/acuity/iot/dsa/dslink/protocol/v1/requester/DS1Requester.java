package com.acuity.iot.dsa.dslink.protocol.v1.requester;

import com.acuity.iot.dsa.dslink.protocol.requester.DSOutboundStub;
import com.acuity.iot.dsa.dslink.protocol.requester.DSRequester;
import com.acuity.iot.dsa.dslink.protocol.v1.CloseMessage;
import com.acuity.iot.dsa.dslink.protocol.v1.DS1Session;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSMap;

/**
 * DSA V1 requester implementation.
 *
 * @author Daniel Shapiro, Aaron Hansen
 */
public class DS1Requester extends DSRequester {

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public DS1Requester(DS1Session session) {
        super(session);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

   /**
     * Call by the parent session to handle response messages.
     */
    public void handleResponse(Integer rid, DSMap map) {
        if (rid == 0) {
            processUpdates(map);
        } else {
            DSOutboundStub stub = requests.get(rid);
            if (stub != null) {
                stub.handleResponse(map);
                if (isError(map)) {
                    stub.handleError(map.get("error"));
                }
                if (isStreamClosed(map)) {
                    stub.handleClose();
                    removeRequest(rid);
                }
            } else {
                if (!isStreamClosed(map)) {
                    sendClose(rid);
                }
            }
        }
    }

    boolean isError(DSMap message) {
        DSElement e = message.get("error");
        if (e == null) {
            return false;
        }
        return !e.isNull();
    }

    boolean isStreamClosed(DSMap message) {
        return "closed".equals(message.getString("stream"));
    }

    public void sendClose(Integer rid) {
        removeRequest(rid);
        sendRequest(new CloseMessage(rid, true));
    }

}
