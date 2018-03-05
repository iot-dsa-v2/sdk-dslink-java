package org.iot.dsa.dslink;

/**
 * Indicates an unsupported method.
 *
 * @author Aaron Hansen
 */
public class DSUnsupportedException extends DSRequestException {

    public DSUnsupportedException() {
    }

    public DSUnsupportedException(String message) {
        super(message);
    }

}
