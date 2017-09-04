package org.iot.dsa.node;

/**
 * A convenience that provides some common default behavior.
 *
 * @author Aaron Hansen
 */
public abstract class DSValue implements DSIValue {

    // Constants
    // ---------

    // Fields
    // ------

    // Constructors
    // ------------

    // Public Methods
    // --------------

    /**
     * Returns this.
     */
    @Override
    public DSIValue copy() {
        return this;
    }

    /**
     * Calls valueOf(arg).
     */
    @Override
    public DSIValue restore(DSElement arg) {
        return valueOf(arg);
    }

    /**
     * Calls toElement()
     */
    @Override
    public DSElement store() {
        return toElement();
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

    // Inner Classes
    // --------------

    // Initialization
    // --------------

}
