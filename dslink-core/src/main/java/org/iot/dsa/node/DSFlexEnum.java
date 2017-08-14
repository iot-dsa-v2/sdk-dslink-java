package org.iot.dsa.node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.iot.dsa.logging.DSLogger;

/**
 * An enum where the range and value are mutable, primarily intended for defining action
 * parameters.
 *
 * @author Aaron Hansen
 */
public class DSFlexEnum extends DSLogger implements DSIEnum, DSIValue, DSIPublisher {

    // Constants
    // ---------

    public static final DSFlexEnum NULL = new DSFlexEnum("null", null);

    // Fields
    // ------

    private DSISubscriber subscriber;
    private String value;
    private ArrayList<String> values;

    // Constructors
    // ------------

    private DSFlexEnum() {
    }

    private DSFlexEnum(String value, String[] values) {
        this.value = value;
        this.values = new ArrayList<String>();
        if (values == null) {
            this.values.add(value);
        } else {
            boolean found = false;
            for (String s : values) {
                this.values.add(s);
                found |= s.equals(value);
            }
            if (!found) {
                this.values.add(0, value);
            }
        }
    }

    // Public Methods
    // --------------

    /**
     * Add a value to the list of potential values and returns this.
     *
     * @return this;
     */
    public DSFlexEnum add(String value) {
        if (!values.contains(value)) {
            values.add(value);
        }
        return this;
    }

    /**
     * Add a value to the list of potential values and returns this.
     *
     * @return this;
     */
    public DSFlexEnum add(String... values) {
        for (String s : values) {
            this.values.add(s);
        }
        if (subscriber != null) {
            try {
                subscriber.onEvent(this, null, Event.PUBLISHER_CHANGED);
            } catch (Exception x) {
                severe(subscriber.toString(), x);
            }
        }
        return this;
    }

    @Override
    public DSFlexEnum copy() {
        DSFlexEnum ret = new DSFlexEnum();
        ret.value = value;
        ret.values = values;
        return ret;
    }

    @Override
    public DSFlexEnum decode(DSElement arg) {
        if ((arg == null) || arg.isNull()) {
            return NULL;
        }
        DSFlexEnum ret = new DSFlexEnum();
        DSMap map = (DSMap) arg;
        ret.value = map.getString("value");
        DSList list = map.getList("values");
        ret.values = new ArrayList<String>();
        for (int i = 0, len = list.size(); i < len; i++) {
            ret.values.add(list.get(i).toString());
        }
        return ret;
    }

    @Override
    public DSElement encode() {
        if (isNull()) {
            return DSString.NULL;
        }
        DSMap ret = new DSMap();
        ret.put("value", value);
        DSList list = new DSList();
        ret.put("values", list);
        for (String s : values) {
            list.add(s);
        }
        return ret;
    }

    /**
     * True if the argument is a DSFlexEnum and the values are equal or they are both isNull.
     */
    @Override
    public boolean equals(Object arg) {
        if (arg == this) {
            return true;
        }
        if (arg instanceof DSFlexEnum) {
            DSFlexEnum denum = (DSFlexEnum) arg;
            if (!value.equals(denum.value)) {
                return false;
            }
            if (!values.equals(denum.values)) {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public List<String> getEnums() {
        return Collections.unmodifiableList(values);
    }

    @Override
    public DSValueType getValueType() {
        return DSValueType.ENUM;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean isNull() {
        return this == null;
    }

    /**
     * Returns true if the given value is in the list of potential values.
     */
    public boolean isValid(String value) {
        return values.contains(value);
    }

    /**
     * Remove a value to the list of potential values and returns this.
     *
     * @return this
     */
    public DSFlexEnum remove(String value) {
        values.remove(value);
        return this;
    }

    /**
     * Sets the current value, will throw an exception if the value is not in the list of potential
     * values.
     *
     * @return this
     */
    public DSFlexEnum setValue(String value) {
        if (!isValid(value)) {
            throw new IllegalStateException("Value not in range: " + value);
        }
        this.value = value;
        if (subscriber != null) {
            try {
                subscriber.onEvent(this, null, Event.PUBLISHER_CHANGED);
            } catch (Exception x) {
                severe(subscriber.toString(), x);
            }
        }
        return this;
    }

    /**
     * Replaces the list of values with the given.
     *
     * @return this
     */
    public DSFlexEnum setValues(String... values) {
        this.values.clear();
        for (String s : values) {
            this.values.add(s);
        }
        if (subscriber != null) {
            try {
                subscriber.onEvent(this, null, Event.PUBLISHER_CHANGED);
            } catch (Exception x) {
                severe(subscriber.toString(), x);
            }
        }
        return this;
    }

    @Override
    public synchronized void subscribe(DSISubscriber arg) {
        if (subscriber == null) {
            subscriber = arg;
        } else if (subscriber instanceof SubscriberAdapter) {
            ((SubscriberAdapter) subscriber).subscribe(arg);
        } else {
            SubscriberAdapter adapter = new SubscriberAdapter(subscriber);
            adapter.subscribe(arg);
            subscriber = adapter;
        }
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public synchronized void unsubscribe(DSISubscriber arg) {
        if (subscriber == arg) {
            subscriber = null;
        } else if (subscriber instanceof SubscriberAdapter) {
            SubscriberAdapter adapter = (SubscriberAdapter) subscriber;
            adapter.unsubscribe(arg);
            if (adapter.isEmpty()) {
                subscriber = null;
            }
        }
    }

    /**
     * Creates a dynamic enum set to the given value and the list of values only contains the
     * given.
     */
    public static DSFlexEnum valueOf(String value) {
        if (value == null) {
            return NULL;
        }
        return new DSFlexEnum(value, null);
    }

    /**
     * Creates a dynamic enum where the list of potential values is set to the arguments and the
     * current value will be the first argument.
     */
    public static DSFlexEnum valueOf(String... values) {
        if (values == null) {
            return NULL;
        }
        return new DSFlexEnum(values[0], values);
    }

    // Inner Classes
    // --------------

    // Initialization
    // --------------

    static {
        DSRegistry.registerNull(DSFlexEnum.class, NULL);
    }

}
