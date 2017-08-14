package org.iot.dsa.dslink.requester;

import java.util.Iterator;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.action.ActionResultSpec;

public interface InboundInvokeResponse {

    public StreamState getStreamState();

    public Iterator<ActionResultSpec> getColumns();

    public Iterator<DSList> getRows();

    public DSMap getMetadata();

}
