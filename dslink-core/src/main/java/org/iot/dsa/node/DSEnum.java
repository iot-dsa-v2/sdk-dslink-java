package org.iot.dsa.node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.iot.dsa.util.DSException;

/**
 * Wrapper for Java enums.
 *
 * @author Aaron Hansen
 */
public class DSEnum implements DSIEnum, DSIValue {

    // Constants
    // ---------

    public static final DSEnum NULL = new DSEnum();

    // Fields
    // ------

    private Enum value;
    private List<String> values;

    // Constructors
    // ------------

    private DSEnum() {
    }

    private DSEnum(Enum value) {
        this.value = value;
    }

    // Public Methods
    // --------------

    @Override
    public DSEnum copy() {
        return this;
    }

    @Override
    public DSEnum decode(DSElement arg) {
        if ((arg == null) || arg.isNull()) {
            return NULL;
        }
        Enum e = null;
        try {
            String s = arg.toString();
            int idx = s.indexOf(':');
            String value = s.substring(0, idx);
            Class clazz = Class.forName(s.substring(++idx));
            e = Enum.valueOf(clazz, value);
        } catch (Exception x) {
            DSException.throwRuntime(x);
        }
        return valueOf(e);
    }

    @Override
    public DSElement encode() {
        if (isNull()) {
            return DSString.NULL;
        }
        return DSString.valueOf(value.name() + ':' + value.getClass().getName());
    }

    /**
     * True if the argument is a DSDynamicEnum and the values are equal or they are both isNull.
     */
    @Override
    public boolean equals(Object arg) {
        if (arg == this) {
            return true;
        }
        if (arg instanceof DSEnum) {
            DSEnum denum = (DSEnum) arg;
            return denum.value.equals(value);
        }
        return false;
    }

    @Override
    public List<String> getEnums() {
        if (values == null) {
            values = new ArrayList<String>();
            for (Enum e : value.getClass().getEnumConstants()) {
                values.add(e.name());
            }
            values = Collections.unmodifiableList(values);
        }
        return values;
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
        return this == NULL;
    }

    /**
     * The Java enum value.
     */
    public Enum toEnum() {
        return value;
    }

    @Override
    public String toString() {
        return value.name();
    }

    /**
     * Creates a dynamic enum set to the given value and the list of values only contains the
     * given.
     */
    public static DSEnum valueOf(Enum value) {
        if (value == null) {
            return NULL;
        }
        return new DSEnum(value);
    }

    // Inner Classes
    // --------------

    // Initialization
    // --------------

    static {
        DSRegistry.registerNull(DSEnum.class, NULL);
    }

}
