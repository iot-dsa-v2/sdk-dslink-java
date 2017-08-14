package org.iot.dsa.dslink.requester;

public enum StreamState {

    INITIALIZED("initialized"),
    OPEN("open"),
    CLOSED("closed");

    private String display;

    StreamState(String display) {
        this.display = display;
    }

    public boolean isInitialized() {
        return this == INITIALIZED;
    }

    public boolean isOpen() {
        return this == OPEN;
    }

    public boolean isClosed() {
        return this == CLOSED;
    }

    @Override
    public String toString() {
        return display;
    }

}
