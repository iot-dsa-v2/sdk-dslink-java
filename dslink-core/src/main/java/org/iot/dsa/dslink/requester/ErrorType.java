package org.iot.dsa.dslink.requester;

import org.iot.dsa.dslink.DSInternalErrorException;
import org.iot.dsa.dslink.DSPermissionException;
import org.iot.dsa.dslink.DSRequestException;
import org.iot.dsa.dslink.DSUnsupportedException;

public enum ErrorType {

    badRequest,
    internalError,
    notSupported,
    permissionDenied;

    public static RuntimeException makeException(ErrorType type, String message) {
        switch (type) {
            case badRequest:
                return new DSRequestException(message);
            case internalError:
                return new DSInternalErrorException(message);
            case notSupported:
                return new DSUnsupportedException(message);
            case permissionDenied:
                return new DSPermissionException(message);
        }
        return new DSInternalErrorException(message);
    }
}
