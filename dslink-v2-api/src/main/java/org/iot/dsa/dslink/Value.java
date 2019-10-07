package org.iot.dsa.dslink;

import org.iot.dsa.node.DSIValue;

/**
 * Used in DSIResponder list responses.
 *
 * @author Aaron Hansen
 */
public interface Value extends Node {

    /**
     * Whether or not the value can be written.
     */
    public boolean isReadOnly();

    /**
     * Does not have to be the current value, used to determine the type
     * of the value.
     * @return
     */
    public DSIValue toValue();

}
