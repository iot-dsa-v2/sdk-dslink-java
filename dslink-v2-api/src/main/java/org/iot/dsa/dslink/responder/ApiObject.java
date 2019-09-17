package org.iot.dsa.dslink.responder;

import java.util.Iterator;
import org.iot.dsa.dslink.Action;
import org.iot.dsa.node.DSMap;

/**
 * Can be a node, value or an action.
 *
 * @author Aaron Hansen
 */
public interface ApiObject {

    /**
     * The action, should only be called if isAction() returns true.
     */
    public Action getAction();

    /**
     * Return the object representing the child with the given name.
     */
    public ApiObject getChild(String name);

    /**
     * Add the children names to the given bucket
     */
    public Iterator<String> getChildren();

    public void getMetadata(DSMap bucket);

    /**
     * True if the object is an action.
     */
    public boolean isAction();

    /**
     * Whether or not this object requires admin permission to read/write.
     */
    public boolean isAdmin();

    /**
     * True if the object should ignored (not be exposed through the api).
     */
    public boolean isPrivate();

    /**
     * True if the object is a value and cannot be written.
     */
    public boolean isReadOnly();

    /**
     * True if the object is a value.
     */
    public boolean isValue();


}
