package com.acuity.iot.dsa.dslink.protocol.responder;

/**
 * Used for lists and subscriptions.
 */
enum StreamState {
    OPEN,
    DISCONNECTED,
    CLOSED;

    public boolean isClosed() {
        return this == CLOSED;
    }

    public boolean isDisconnected() {
        return this == DISCONNECTED;
    }

    public boolean isOpen() {
        return this == OPEN;
    }

}
