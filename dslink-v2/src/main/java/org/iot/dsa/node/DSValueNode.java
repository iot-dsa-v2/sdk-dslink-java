package org.iot.dsa.node;

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
    public abstract DSInfo getValueChild();

    @Override
    public DSValueType getValueType() {
        return getValueChild().getValue().getValueType();
    }

    /**
     * This fires the VALUE_CHANGED topic when the value child changes.  Overrides should call
     * super.onChildChanged.
     */
    @Override
    public void onChildChanged(DSInfo child) {
        DSInfo info = getValueChild();
        if (child == info) {
            fire(VALUE_CHANGED_TOPIC, child, null);
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
