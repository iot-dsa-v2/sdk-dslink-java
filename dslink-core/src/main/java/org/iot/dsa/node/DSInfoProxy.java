package org.iot.dsa.node;

import org.iot.dsa.util.DSUtil;

/**
 * DSInfo that proxies the info of a default instance.
 *
 * @author Aaron Hansen
 */
class DSInfoProxy extends DSInfo {

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSInfo defaultInfo;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    DSInfoProxy(DSInfo defaultInfo) {
        this.defaultInfo = defaultInfo;
        this.flags = defaultInfo.flags;
        this.name = defaultInfo.name;
        if (defaultInfo.getObject() != null) {
            setObject(defaultInfo.getObject().copy());
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public DSInfo copy() {
        DSInfoProxy ret = new DSInfoProxy(defaultInfo);
        ret.flags = flags;
        ret.name = name;
        ret.defaultInfo = defaultInfo;
        if (value != null) {
            ret.setObject(value.copy());
        }
        return ret;
    }

    /**
     * The instance value if it differs from the default, otherwise the default.
     */
    public DSIObject getObject() {
        if (value == null) {
            return null;
        }
        return value;
    }

    @Override
    public boolean equalsDefault() {
        return equalsDefaultState() && equalsDefaultValue();
    }

    @Override
    public boolean equalsDefaultState() {
        return flags == defaultInfo.flags;
    }

    @Override
    public boolean equalsDefaultType() {
        if (value == null) {
            return defaultInfo.value == null;
        }
        if (defaultInfo.value == null) {
            return false;
        }
        return DSUtil.equal(value.getClass(), defaultInfo.value.getClass());
    }

    @Override
    public boolean equalsDefaultValue() {
        return DSUtil.equal(value, defaultInfo.value);
    }

    @Override
    boolean isProxy() {
        return true;
    }

}
