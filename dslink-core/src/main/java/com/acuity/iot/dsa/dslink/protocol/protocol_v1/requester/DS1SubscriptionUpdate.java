package com.acuity.iot.dsa.dslink.protocol.protocol_v1.requester;

import org.iot.dsa.dslink.requester.InboundSubscriptionUpdate;
import org.iot.dsa.node.DSElement;

public class DS1SubscriptionUpdate implements InboundSubscriptionUpdate {

    private DSElement value;
    private String timestamp;
    private String status;
    private Integer count;
    private Number sum, min, max;

    @Override
    public DSElement getValue() {
        return value;
    }

    @Override
    public String getTimestamp() {
        return timestamp;
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public Integer getCount() {
        return count;
    }

    @Override
    public Number getSum() {
        return sum;
    }

    @Override
    public Number getMin() {
        return min;
    }

    @Override
    public Number getMax() {
        return max;
    }

    public void setValue(DSElement value) {
        this.value = value;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public void setSum(Number sum) {
        this.sum = sum;
    }

    public void setMin(Number min) {
        this.min = min;
    }

    public void setMax(Number max) {
        this.max = max;
    }

}
