package org.iot.dsa.node;

import org.iot.dsa.node.event.DSEvent;

/**
 * A convenience implementation of a node that is also a value.  The value of the node must be
 * stored as a child.  Subclasses only need to override is getValueChild()
 *
 * @author Aaron Hansen
 * @see DSValueNode#getValueChild()
 */
public abstract class DSValueNode extends DSNode implements DSIValue {

    /**
     * Subclasses must store the node value in a child value and provide the info for that child
     * here.  This method will be called often, it would be best to cache the info instance
     * rather then doing a name lookup each time.
     */
    public abstract DSInfo<DSIValue> getValueChild();

    @Override
    public DSValueType getValueType() {
        return getValueChild().getValue().getValueType();
    }

    @Override
    public boolean isNull() {
        return getValueChild().isNull();
    }

    /**
     * This fires the VALUE_CHANGED event when the value child changes, on this node and the parent
     * node.  Overrides should call super.onChildChanged.
     */
    @Override
    public void onChildChanged(DSInfo child) {
        DSInfo info = getValueChild();
        if (child == info) {
            fire(VALUE_CHANGED_EVENT, child, child.getValue());
            DSNode parent = getParent();
            if (parent != null) {
                parent.fire(VALUE_CHANGED_EVENT, getInfo(), child.getValue());
            }
        }
    }

    @Override
    public void onSet(DSIValue value) {
        put(getValueChild(), value);
    }

    @Override
    public DSElement toElement() {
        return getValueChild().getValue().toElement();
    }

    @Override
    public DSIValue valueOf(DSElement element) {
        return getValueChild().getValue().valueOf(element);
    }

}
