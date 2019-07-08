package org.iot.dsa.dslink;

/**
 * Indicates an unsupported method.
 *
 * @author Aaron Hansen
 */
public class DSInternalErrorException extends RuntimeException {

    public DSInternalErrorException() {
    }

    public DSInternalErrorException(String message) {
        super(message);
    }

}
