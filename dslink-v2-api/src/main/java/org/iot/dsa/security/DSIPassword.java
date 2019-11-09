package org.iot.dsa.security;

import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSIValue;

/**
 * Defines the api for verifying passwords.
 *
 * @author Aaron Hansen
 */
public interface DSIPassword extends DSIValue {

    // Public Methods
    // --------------

    /**
     * Returns true if the give element is valid according to the backing implementation.
     */
    boolean isValid(DSElement arg);

}
