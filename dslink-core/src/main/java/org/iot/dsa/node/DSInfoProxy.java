package org.iot.dsa.node;

import org.iot.dsa.util.DSUtil;

/**
 * DSInfo that proxies the info of a default instance.
 *
 * @author Aaron Hansen
 */
class DSInfoProxy extends DSInfo {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

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
    public boolean isDefault() {
        return isDefaultState() && isDefaultValue();
    }

    @Override
    public boolean isDefaultState() {
        if (flags != defaultInfo.flags) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isDefaultType() {
        if (value == null) {
            return defaultInfo.value == null;
        }
        if (defaultInfo.value == null) {
            return false;
        }
        return DSUtil.equal(value.getClass(), defaultInfo.value.getClass());
    }

    @Override
    public boolean isDefaultValue() {
        return DSUtil.equal(value, defaultInfo.value);
    }

    @Override
    boolean isProxy() {
        return true;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Initialization
    ///////////////////////////////////////////////////////////////////////////

} //class
