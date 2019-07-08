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
        if (defaultInfo.get() != null) {
            setObject(defaultInfo.get().copy());
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
        if (metadata != null) {
            ret.metadata = metadata.copy();
        }
        if (isDefaultOnCopy()) {
            DSIObject val = getDefaultObject();
            if (val != null) {
                ret.setObject(val.copy());
            }
        } else if (object != null) {
            ret.setObject(object.copy());
        }
        return ret;
    }

    @Override
    public boolean equalsDefault() {
        return equalsDefaultState() && equalsDefaultMetadata() && equalsDefaultValue();
    }

    @Override
    public boolean equalsDefaultMetadata() {
        if (metadata == null) {
            return true;
        } else if (defaultInfo.metadata == null) {
            return metadata.isEmpty();
        }
        return metadata.equals(defaultInfo.metadata);
    }

    @Override
    public boolean equalsDefaultState() {
        return flags == defaultInfo.flags;
    }

    @Override
    public boolean equalsDefaultType() {
        if (object == null) {
            return defaultInfo.object == null;
        }
        if (defaultInfo.object == null) {
            return false;
        }
        return DSUtil.equal(object.getClass(), defaultInfo.object.getClass());
    }

    @Override
    public boolean equalsDefaultValue() {
        return DSUtil.equal(object, defaultInfo.object);
    }

    @Override
    public DSIObject getDefaultObject() {
        return defaultInfo.get();
    }

    @Override
    public void getMetadata(DSMap bucket) {
        if (metadata != null) {
            bucket.putAll(metadata);
            return;
        }
        defaultInfo.getMetadata(bucket);
    }

    @Override
    public DSMetadata getMetadata() {
        if (metadata != null) {
            return new DSMetadata(metadata);
        }
        metadata = new DSMap();
        metadata.setParent(this);
        defaultInfo.getMetadata(metadata);
        return new DSMetadata(metadata);
    }

    @Override
    void copy(DSInfo info) {
        if (info instanceof DSInfoProxy) {
            defaultInfo = ((DSInfoProxy) info).defaultInfo;
        } else {
            defaultInfo = info;
        }
        super.copy(info);
    }

    @Override
    boolean isProxy() {
        return true;
    }

}
