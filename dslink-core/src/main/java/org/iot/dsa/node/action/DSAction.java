package org.iot.dsa.node.action;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.iot.dsa.node.DSIMetadata;
import org.iot.dsa.node.DSIObject;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSMetadata;
import org.iot.dsa.node.DSValueType;
import org.iot.dsa.security.DSPermission;

/**
 * Fully describes an action and routes invocations to DSNode.onInvoke.
 *
 * @author Aaron Hansen
 * @see org.iot.dsa.node.DSNode#onInvoke(DSInfo, ActionInvocation)
 */
public class DSAction implements ActionSpec, DSIObject {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Use this when you have no-arg, no-return actions.  This instance cannot be modified.
     */
    public static final DSAction DEFAULT = new DSAction();

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private List<DSMap> parameters;
    private DSPermission permission = DSPermission.READ;
    private ResultType result = ResultType.VOID;
    private List<DSMap> valueResults;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Methods in alphabetical order
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public DSAction copy() {
        return this;
    }

    /**
     * A convenience which calls addParameter with the same arguments, and also sets the metadata
     * for default value.
     *
     * @param name        Must not be null.
     * @param value       Must not be null.
     * @param description Can be null.
     * @return Metadata for further configuration.
     */
    public DSMetadata addDefaultParameter(String name, DSIValue value, String description) {
        if (this == DEFAULT) {
            throw new IllegalStateException("Cannot modify the default action.");
        }
        return addParameter(name, value, description).setDefault(value);
    }

    /**
     * Fully describes a parameter for method invocation.  At the very least, the map should have a
     * unique name and a value type, use the metadata utility class to build the map.
     *
     * @return This.
     * @see DSMetadata
     */
    public DSAction addParameter(DSMap metadata) {
        if (this == DEFAULT) {
            throw new IllegalStateException("Cannot modify the default action.");
        }
        if (parameters == null) {
            parameters = new ArrayList<DSMap>();
        }
        validate(metadata, parameters);
        parameters.add(metadata);
        return this;
    }

    /**
     * Creates a DSMetadata, calls setName and setType on it, adds the internal map to the parameter
     * list and returns the metadata instance for further configuration.
     *
     * @param name        Must not be null.
     * @param value       Must not be null.
     * @param description Can be null.
     * @return Metadata for further configuration.
     */
    public DSMetadata addParameter(String name, DSIValue value, String description) {
        if (this == DEFAULT) {
            throw new IllegalStateException("Cannot modify the default action.");
        }
        DSMetadata ret = new DSMetadata();
        if (value instanceof DSIMetadata) {
            ((DSIMetadata) value).getMetadata(ret.getMap());
        }
        ret.setName(name)
           .setType(value)
           .setDescription(description);
        addParameter(ret.getMap());
        return ret;
    }

    /**
     * Creates a DSMetadata, calls setName and setType on it, adds the internal map to the parameter
     * list and returns the metadata instance for further configuration.
     *
     * @param name        Must not be null.
     * @param type        Must not be null.
     * @param description Can be null.
     * @return Metadata for further configuration.
     */
    public DSMetadata addParameter(String name, DSValueType type, String description) {
        if (this == DEFAULT) {
            throw new IllegalStateException("Cannot modify the default action.");
        }
        DSMetadata ret = new DSMetadata();
        ret.setName(name)
           .setType(type)
           .setDescription(description);
        addParameter(ret.getMap());
        return ret;
    }

    /**
     * Fully describes a return value when the result type is VALUES.  Must be added in the order
     * that the values will be returned. At the very least, the map should have a unique name and a
     * value type, use the DSMetadata utility class.
     *
     * @return This.
     * @see DSMetadata
     */
    public DSAction addValueResult(DSMap metadata) {
        if (this == DEFAULT) {
            throw new IllegalStateException("Cannot modify the default action.");
        }
        if (valueResults == null) {
            valueResults = new ArrayList<DSMap>();
        }
        validate(metadata, valueResults);
        valueResults.add(metadata);
        return this;
    }

    /**
     * Creates a DSMetadata, calls setName and setType on it, adds the internal map to the results
     * list and returns the metadata instance for further configuration.
     *
     * @param name  Must not be null.
     * @param value Must not be null.
     * @return Metadata for further configuration.
     */
    public DSMetadata addValueResult(String name, DSIValue value) {
        if (this == DEFAULT) {
            throw new IllegalStateException("Cannot modify the default action.");
        }
        DSMetadata ret = new DSMetadata();
        if (value instanceof DSIMetadata) {
            ((DSIMetadata) value).getMetadata(ret.getMap());
        }
        ret.setName(name)
           .setType(value);
        addValueResult(ret.getMap());
        return ret;
    }

    /**
     * Creates a DSMetadata, calls setName and setType on it, adds the internal map to the results
     * list and returns the metadata instance for further configuration.
     *
     * @param name Must not be null.
     * @param type Must not be null.
     * @return Metadata for further configuration.
     */
    public DSMetadata addValueResult(String name, DSValueType type) {
        if (this == DEFAULT) {
            throw new IllegalStateException("Cannot modify the default action.");
        }
        DSMetadata ret = new DSMetadata();
        ret.setName(name)
           .setType(type);
        addValueResult(ret.getMap());
        return ret;
    }

    @Override
    public Iterator<DSMap> getParameters() {
        if (parameters != null) {
            ArrayList<DSMap> tmp = new ArrayList<DSMap>(parameters.size());
            tmp.addAll(parameters);
            return tmp.iterator();
        }
        return null;
    }

    @Override
    public DSPermission getPermission() {
        return permission;
    }

    @Override
    public ResultType getResultType() {
        return result;
    }

    @Override
    public Iterator<DSMap> getValueResults() {
        return valueResults.iterator();
    }

    /**
     * Calls onInvoke on the proper node.  If this is overridden, do not use the outer 'this' of
     * inner classes since that will point to the default instance which should not be touched. This
     * can be called on actions stored on default instances, so the actual target of the invocation
     * is passed as a parameter.
     *
     * @param info       The info about the action in it's parent container.  Never use the outer
     *                   'this' of anonymous instances.
     * @param invocation Details about the incoming invoke as well as the mechanism to send updates
     *                   over an open stream.
     * @return Can be null if the result type is void.
     * @throws IllegalStateException If the target node has not overridden onInvoke.
     */
    public ActionResult invoke(DSInfo info, ActionInvocation invocation) {
        return info.getParent().onInvoke(info, invocation);
    }

    /**
     * Defaults to the equals method.
     */
    @Override
    public boolean isEqual(Object obj) {
        return equals(obj);
    }

    /**
     * False
     */
    @Override
    public boolean isNull() {
        return false;
    }

    /**
     * Override point, called for each parameter as it is being sent to the requester.  Allows you
     * for example, to use current values as defaults.
     *
     * @param info      The info about the action in it's parent container.  Never use the outer
     *                  'this' of anonymous instances.
     * @param parameter Map representing a single parameter.
     */
    public void prepareParameter(DSInfo info, DSMap parameter) {
    }

    /**
     * Returns this, it is not necessary to set the permission to read.
     */
    public DSAction setPermission(DSPermission permission) {
        if (this == DEFAULT) {
            throw new IllegalStateException("Cannot modify the default action.");
        }
        this.permission = permission;
        return this;
    }

    /**
     * Returns this, it is not necessary to set the result to void.
     */
    public DSAction setResultType(ResultType result) {
        if (this == DEFAULT) {
            throw new IllegalStateException("Cannot modify the default action.");
        }
        this.result = result;
        return this;
    }

    /**
     * Ensure name and type, and prevent duplicate names. Prevent null and duplicates.
     */
    private void validate(DSMap params, List<DSMap> existing) {
        if (params.isEmpty()) {
            throw new IllegalArgumentException("Empty metadata");
        }
        String name = params.getString("name");
        if ((name == null) || name.isEmpty()) {
            throw new IllegalArgumentException("Missing name");
        }
        if (params.getString(DSMetadata.TYPE) == null) {
            throw new IllegalArgumentException("Missing type");
        }
        if (parameters == null) {
            return;
        }
        for (DSMap param : existing) {
            if (name.equals(param.getString("name"))) {
                throw new IllegalArgumentException("Duplicate name: " + name);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Initialization
    ///////////////////////////////////////////////////////////////////////////

}
