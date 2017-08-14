package org.iot.dsa.node.action;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.iot.dsa.node.DSIObject;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSValueType;
import org.iot.dsa.security.DSPermission;
import org.iot.dsa.util.DSMetadata;

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

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private List<DSMap> parameters;
    private DSPermission permission = DSPermission.READ;
    private ResultType result = ResultType.VOID;
    private List<ActionResultSpec> valueResults;

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
     * The most low-level and flexible way to create an action parameter.  At the very least, the
     * parameter should have a unique name and a value type, see the Metadata utility class.
     *
     * @see DSMetadata
     */
    public DSAction addParameter(DSMap param) {
        if (parameters == null) {
            parameters = new ArrayList<DSMap>();
        }
        validate(param);
        parameters.add(param);
        return this;
    }

    /**
     * Creates a Metadata, calls setName and setDefault on it, adds the internal map to the
     * parameter list and returns the Metadata instance for further configuration.
     *
     * @param name        Must not be null.
     * @param value       Must not be null.
     * @param description Can be null.
     * @return Metadata for further configuration.
     */
    public DSMetadata addParameter(String name, DSIValue value, String description) {
        DSMetadata ret = new DSMetadata().setName(name)
                                         .setDefault(value)
                                         .setDescription(description);
        addParameter(ret.getMap());
        return ret;
    }

    /**
     * Only needed when the result type is VALUES.
     *
     * @param name     Required
     * @param type     Required
     * @param metadata Optional
     * @return This.
     */
    public DSAction addValueResult(String name, DSValueType type, DSMap metadata) {
        if (valueResults == null) {
            valueResults = new ArrayList<ActionResultSpec>();
        }
        if (name == null) {
            throw new NullPointerException("Name cannot be null");
        } else if (type == null) {
            throw new NullPointerException("Type cannot be null");
        }
        valueResults.add(new ValueResult(name, type, metadata));
        return this;
    }

    @Override
    public Iterator<DSMap> getParameters() {
        if (parameters != null) {
            return parameters.iterator();
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
    public Iterator<ActionResultSpec> getValueResults() {
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
     * False
     */
    @Override
    public boolean isNull() {
        return false;
    }

    /**
     * Returns this, it is not necessary to set the permission to read.
     */
    public DSAction setPermission(DSPermission permission) {
        this.permission = permission;
        return this;
    }

    /**
     * Returns this, it is not necessary to set the result to void.
     */
    public DSAction setResultType(ResultType result) {
        this.result = result;
        return this;
    }

    /**
     * Prevent null and duplicates.
     */
    private void validate(DSMap params) {
        if (params.isEmpty()) {
            throw new IllegalArgumentException("Missing parameter name");
        }
        String name = params.getString("name");
        if (name == null) {
            throw new IllegalArgumentException("Missing parameter name");
        }
        if (params.getString(DSMetadata.TYPE) == null) {
            if (params.getString(DSMetadata.DEFAULT) == null) {
                throw new IllegalArgumentException("Missing parameter default value or type");
            }
        }
        if (parameters == null) {
            return;
        }
        for (DSMap param : parameters) {
            if (name.equals(param.getString("name"))) {
                throw new IllegalArgumentException("Duplicate parameter name: " + name);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    private static class ValueResult implements ActionResultSpec {

        private String name;
        private DSValueType valueType;
        private DSMap metadata;

        ValueResult(String name, DSValueType valueType, DSMap metadata) {
            this.name = name;
            this.valueType = valueType;
            this.metadata = metadata;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public DSMap getMetadata() {
            return metadata;
        }

        @Override
        public DSValueType getType() {
            return valueType;
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Initialization
    ///////////////////////////////////////////////////////////////////////////

}
