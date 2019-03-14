package org.iot.dsa.dslink;

import org.iot.dsa.node.DSIObject;

/**
 * Encapsulates the state of a connection well as the protocol implementation.
 *
 * @author Aaron Hansen
 */
public interface DSISession extends DSIObject {

    public DSLinkConnection getConnection();

    public DSIRequester getRequester();

    public boolean isRequesterAllowed();

    /**
     * The transport should call this at the start of each message.
     */
    public void recvMessage();

}
