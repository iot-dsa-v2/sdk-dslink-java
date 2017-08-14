package org.iot.dsa.dslink.responder;

import java.util.Iterator;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.action.ActionSpec;

/**
 * Can be a node, value or an action.
 *
 * @author Aaron Hansen
 */
public interface ApiObject {

    /**
     * The action, should only be called if isAction() returns true.
     */
    public ActionSpec getAction();

    /**
     * Iterator of child objects, should only be called if hasChildren() returns true.
     */
    public Iterator<ApiObject> getChildren();

    /**
     * Add any meta-data for the object to the given map.
     */
    public void getMetadata(DSMap metadata);

    /**
     * The display name.
     */
    public String getName();

    /**
     * Value of the object, should only be called if isValue() returns true.
     */
    public DSIValue getValue();

    /**
     * True if getChildren() can be called.
     */
    public boolean hasChildren();

    /**
     * True if the object is an action.
     */
    public boolean isAction();

    /**
     * Whether or not this object requires configuration permission to read/write.
     */
    public boolean isConfig();

    /**
     * True if the object should ignored (not be exposed through the api).
     */
    public boolean isHidden();

    /**
     * True if the object is a value and cannot be written.
     */
    public boolean isReadOnly();

    /**
     * True if getValue() can be called.
     */
    public boolean isValue();


}
