package org.iot.dsa.node;

/**
 * Enables custom serialization for non-node values in the configuration database.  Not used
 * for DSA interop.
 *
 * @author Aaron Hansen
 */
public interface DSIStorable {

    /**
     * Deserialize a value from the configuration database, these will be values returned from the
     * store() method.
     */
    DSIValue restore(DSElement element);

    /**
     * Serialize the value for the configuration database.  Can be a different element type than
     * toElement().
     */
    DSElement store();

}
