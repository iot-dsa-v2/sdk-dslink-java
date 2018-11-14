package org.iot.dsa.node;

class DynamicInfo extends DSInfo {

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public DynamicInfo(String name, DSIObject obj) {
        super(name, obj);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Package / Private Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    boolean isDynamic() {
        return true;
    }

}
