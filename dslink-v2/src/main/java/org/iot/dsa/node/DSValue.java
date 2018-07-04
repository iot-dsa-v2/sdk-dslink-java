package org.iot.dsa.node;

/**
 * A convenience that provides some common default behavior.
 *
 * @author Aaron Hansen
 */
public abstract class DSValue implements DSIValue {

    /**
     * Returns this.
     */
    @Override
    public DSIValue copy() {
        return this;
    }

    /**
     * If isNull(), returns "null", otherwise returns toElement().toString()
     */
    @Override
    public String toString() {
        if (isNull()) {
            return "null";
        }
        return toElement().toString();
    }

}
