package org.iot.dsa.node.action;

import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSValueType;

/**
 * Defines a column in an ActionTable that is returned from an action invocation.
 *
 * @author Aaron Hansen
 */
public interface ActionResultSpec {

    /**
     * Meta-data for the column, or null.
     */
    public DSMap getMetadata();

    /**
     * Column display name.
     */
    public String getName();

    /**
     * The type of the column values.
     */
    public DSValueType getType();

}
