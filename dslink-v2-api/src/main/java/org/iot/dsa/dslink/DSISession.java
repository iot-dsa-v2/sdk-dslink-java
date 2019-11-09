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
     * Read the next message.  Intended to be called by the transport.
     *
     * @param async If false, the messsage will be read on the calling thread.
     */
    public void recvMessage(boolean async);

    /**
     * An object has been added to the tree, use this to update existing list and
     * subscription requests for everything starting with this path.
     */
    public void update(String path);

}
