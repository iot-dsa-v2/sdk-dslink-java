package com.acuity.iot.dsa.dslink.protocol.protocol_v1.requester;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.iot.dsa.dslink.requester.InboundInvokeResponse;
import org.iot.dsa.dslink.requester.StreamState;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.action.ActionResultSpec;

public class DS1InboundInvokeResponse implements InboundInvokeResponse {

    private StreamState streamState;
    private final List<ActionResultSpec> columns = new ArrayList<ActionResultSpec>();
    private final List<DSList> rows = new ArrayList<DSList>();
    private DSMap metadata;

    public void setStreamState(StreamState streamState) {
        this.streamState = streamState;
    }

    @Override
    public StreamState getStreamState() {
        return streamState;
    }

    public void addColumn(ActionResultSpec col) {
        columns.add(col);
    }

    @Override
    public Iterator<ActionResultSpec> getColumns() {
        return columns.iterator();
    }

    public void addRow(DSList row) {
        rows.add(row);
    }

    @Override
    public Iterator<DSList> getRows() {
        return rows.iterator();
    }

    public void setMetadata(DSMap metadata) {
        this.metadata = metadata;
    }

    @Override
    public DSMap getMetadata() {
        return metadata;
    }

}
