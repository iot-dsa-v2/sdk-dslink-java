package org.iot.dsa.conn;

import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSIEnum;
import org.iot.dsa.node.DSIObject;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSRegistry;
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.DSValueType;

public enum DSConnectionState implements DSIEnum, DSIValue {

    CONNECTED("Connected"),
    CONNECTING("Connecting"),
    DISCONNECTED("Disconnected"),
    DISCONNECTING("Disconnecting");

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSString element;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    private DSConnectionState(String display) {
        this.element = DSString.valueOf(display);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public DSIObject copy() {
        return this;
    }

    @Override
    public DSList getEnums(DSList bucket) {
        if (bucket == null) {
            bucket = new DSList();
        }
        for (DSConnectionState e : values()) {
            bucket.add(e.toElement());
        }
        return bucket;
    }

    @Override
    public DSValueType getValueType() {
        return DSValueType.ENUM;
    }

    public boolean isConnected() {
        return this == CONNECTED;
    }

    public boolean isConnecting() {
        return this == CONNECTING;
    }

    public boolean isDisconnected() {
        return this == DISCONNECTED;
    }

    public boolean isDisconnecting() {
        return this == DISCONNECTING;
    }

    /**
     * True if disconnecting or disconnected.
     */
    public boolean isDisengaged() {
        return isDisconnected() || isDisconnecting();
    }

    /**
     * True if connecting or connected.
     */
    public boolean isEngaged() {
        return isConnected() || isConnecting();
    }

    @Override
    public boolean isEqual(Object obj) {
        if (obj == this) {
            return true;
        }
        return element.equals(obj);
    }

    @Override
    public boolean isNull() {
        return false;
    }

    @Override
    public DSElement toElement() {
        return element;
    }

    @Override
    public DSIValue valueOf(DSElement element) {
        for (DSConnectionState e : values()) {
            if (e.toElement().equals(element)) {
                return e;
            }
        }
        return null;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Initialization
    ///////////////////////////////////////////////////////////////////////////

    static {
        DSRegistry.registerDecoder(DSConnectionState.class, CONNECTED);
    }

}
