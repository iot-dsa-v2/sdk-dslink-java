package org.iot.dsa.node.event;

/**
 * Basic implementation of DSITopic.
 *
 * @author Aaron Hansen
 */
public class DSTopic implements DSITopic {

    /**
     * Returns this.
     */
    @Override
    public DSTopic copy() {
        return this;
    }

    /**
     * Only tests instance equality.
     */
    @Override
    public boolean isEqual(Object obj) {
        return obj == this;
    }

    /**
     * False
     */
    @Override
    public boolean isNull() {
        return false;
    }

}
