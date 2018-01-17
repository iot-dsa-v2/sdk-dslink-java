package org.iot.dsa.node;

import org.iot.dsa.node.event.DSTopic;
import org.iot.dsa.node.event.DSValueTopic.Event;

/**
 * A convenience implementation of a node that is also values.  The value of the node must be stored
 * in a child and the only method to override is getValueChild()
 *
 * @author Aaron Hansen
 * @see DSValueNode#getValueChild()
 */
public abstract class DSValueNode extends DSNode implements DSIValue {

    // Fields
    // ------

    private DSIValue prev;


    // Methods
    // -------

    /**
     * This fires the NODE_CHANGED topic when the value child changes.  Overrides should call
     * super.onChildChanged.
     */
    @Override
    public void onChildChanged(DSInfo child) {
        DSInfo info = getValueChild();
        if (child == info) {
            DSIValue val = info.getValue();
            fire(null, VALUE_TOPIC, Event.NODE_CHANGED, prev, val);
            prev = val;
        }
    }

    @Override
    public DSValueType getValueType() {
        return getValueChild().getValue().getValueType();
    }

    /**
     * Subclasses must store the node value in a child value and provide the info for that child
     * here.  This method will be called often, it would be best to cache the info instance
     * rather then doing a name lookup each time.
     */
    public abstract DSInfo getValueChild();

    @Override
    public DSElement toElement() {
        return getValueChild().getValue().toElement();
    }

    @Override
    public void onSet(DSIValue value) {
        put(getValueChild(), value);
    }

    /**
     * Captures the current value of the value child when the node is subscribed for value changes.
     * Overrides should call super.onSubscribed.
     */
    @Override
    public void onSubscribed(DSInfo child, DSTopic topic) {
        if (topic == VALUE_TOPIC) {
            DSInfo info = getValueChild();
            if (child == null) {
                prev = info.getValue();
            }
        }
    }

    @Override
    public DSIValue valueOf(DSElement element) {
        return getValueChild().getValue().valueOf(element);
    }

}
