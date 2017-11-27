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
    public DSInfo clearMetadata() {
        if (metadata == null) {
            metadata = new DSMap();
        } else {
            metadata.clear();
        }
        return this;
    }

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

    public boolean hasMetadata() {
        if (metadata == null) {
            return defaultInfo.hasMetadata();
        }
        return !metadata.isEmpty();
    }

    @Override
    public boolean equalsDefault() {
        return equalsDefaultState() && equalsDefaultValue() && equalsDefaultMetadata();
    }

    @Override
    public boolean equalsDefaultMetadata() {
        if (metadata == null) {
            return true;
        }
        return metadata.equals(defaultInfo.metadata);
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

    @Override
    public void getMetadata(DSMap bucket) {
        if (metadata == null) {
            defaultInfo.getMetadata(bucket);
        } else {
            bucket.putAll(metadata);
        }
    }

    /**
     * Adds all the metadata to the info and returns this.
     */
    public DSInfo putMetadata(DSMap map) {
        if (metadata == null) {
            metadata = new DSMap();
            defaultInfo.getMetadata(metadata);
        }
        metadata.putAll(map);
        return this;
    }

    /**
     * Adds the metadata to the info and returns this.
     */
    public DSInfo putMetadata(String name, DSElement value) {
        if (metadata == null) {
            metadata = new DSMap();
            defaultInfo.getMetadata(metadata);
        }
        metadata.put(name, value);
        return this;
    }

    /**
     * Returns the removed value, or null.
     */
    public DSElement removeMetadata(String name) {
        if (metadata == null) {
            metadata = new DSMap();
            defaultInfo.getMetadata(metadata);
        }
        return metadata.remove(name);
    }


    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Initialization
    ///////////////////////////////////////////////////////////////////////////

} //class
