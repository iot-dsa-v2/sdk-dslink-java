package org.iot.dsa.dslink;

/**
 * Indicates a request has insufficient permissions.
 *
 * @author Aaron Hansen
 */
public class DSPermissionException extends DSRequestException {

    public DSPermissionException(String message) {
        super(message);
    }

}
