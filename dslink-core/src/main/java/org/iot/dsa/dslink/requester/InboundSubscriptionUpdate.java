package org.iot.dsa.dslink.requester;

import org.iot.dsa.node.DSElement;

public interface InboundSubscriptionUpdate {

    /**
     * The updated value
     *
     * @return Possibly null
     */
    public DSElement getValue();

    /**
     * Last update time of the value
     */
    public String getTimestamp();

    /**
     * Status of the value ok - No issues with the node value (default is status is omitted) stale -
     * The value could potentially be out of date disconnected - Never set by a dslink. The broker
     * sets this if a connection is lost between the dslink and the broker.
     *
     * @return Possibly null
     */
    public String getStatus();

    /**
     * If the response skips some values, this shows how many updates have happened since last
     * response
     *
     * @return Usually null
     */
    public Integer getCount();

    /**
     * The sum value if one or more numeric value is skipped
     *
     * @return Usually null
     */
    public Number getSum();

    /**
     * The min value if one or more numeric value is skipped
     *
     * @return Usually null
     */
    public Number getMin();

    /**
     * The max value if one or more numeric value is skipped
     *
     * @return Usually null
     */
    public Number getMax();
}
