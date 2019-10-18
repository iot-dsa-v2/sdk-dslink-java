package org.iot.dsa.node.action;

import java.util.ArrayList;
import java.util.List;
import org.iot.dsa.dslink.ActionResults;
import org.iot.dsa.node.DSIMetadata;
import org.iot.dsa.node.DSIObject;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSMetadata;

/**
 * A convenience implementation of DSIAction.
 *
 * @author Aaron Hansen
 */
public class DSAction implements DSIAction, DSIMetadata, DSIObject {

    ///////////////////////////////////////////////////////////////////////////
    // Class Fields
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Simple action stub that does nothing.  Intended for use by overriding DNode.invoke
     * and handling action there.
     */
    public static final DSAction DEFAULT = new DSAction();

    public static final String EDIT_GROUP = "Edit";
    public static final String NEW_GROUP = "New";

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    private String actionGroup;
    private String actionGroupDisplay;
    private List<DSMap> columns;
    private boolean immutable = false;
    private List<DSMap> parameters;
    private ResultsType result = ResultsType.VOID;

    ///////////////////////////////////////////////////////////////////////////
    // Methods in alphabetical order
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Fully describes a return value when the result type is VALUES.  Must be added in
     * the order that the values will be returned. At the very least, the map should have
     * a unique name and a value type, use the DSMetadata utility class.
     *
     * @return This.
     * @see DSMetadata
     */
    public DSAction addColumnMetadata(DSMap metadata) {
        if (immutable) {
            throw new IllegalStateException("Action is immutable");
        }
        if (columns == null) {
            columns = new ArrayList<>();
        }
        validate(metadata, columns);
        columns.add(metadata);
        return this;
    }

    /**
     * Creates a DSMetadata, calls setName and setType on it, adds the internal map to
     * the results list and returns the metadata instance for further configuration.
     *
     * @param name  Must not be null.
     * @param value Must not be null.
     * @return Metadata for further configuration.
     */
    public DSMetadata addColumnMetadata(String name, DSIValue value) {
        if (immutable) {
            throw new IllegalStateException("Action is immutable");
        }
        DSMetadata ret = new DSMetadata();
        ret.setName(name)
           .setType(value);
        if (value instanceof DSIMetadata) {
            ((DSIMetadata) value).getMetadata(ret.getMap());
        }
        addColumnMetadata(ret.getMap());
        return ret;
    }

    /**
     * A convenience which calls addParameter with the same arguments, but also sets the default
     * value of the parameter to the given as well.
     *
     * @param name        Must not be null.
     * @param value       Must not be null.
     * @param description Can be null.
     * @return Metadata for further configuration.
     */
    public DSMetadata addDefaultParameter(String name, DSIValue value, String description) {
        if (immutable) {
            throw new IllegalStateException("Action is immutable");
        }
        return addParameter(name, value, description).setDefault(value);
    }

    /**
     * Fully describes a parameter for method invocation.  At the very least, the map
     * should have a unique name and a value type.  You should use the metadata utility
     * class to build the map.
     *
     * @return This.
     * @see DSMetadata
     */
    public DSAction addParameter(DSMap metadata) {
        if (immutable) {
            throw new IllegalStateException("Action is immutable");
        }
        if (parameters == null) {
            parameters = new ArrayList<DSMap>();
        }
        validate(metadata, parameters);
        parameters.add(metadata);
        return this;
    }

    /**
     * Creates a DSMetadata, calls setName and setType on it, adds the internal map to
     * the parameter list and returns the metadata instance for further configuration.  Does
     * not set the default value of the parameter.
     *
     * @param name        Must not be null.
     * @param value       Must not be null.
     * @param description Can be null.
     * @return Metadata for further configuration.
     */
    public DSMetadata addParameter(String name, DSIValue value, String description) {
        if (immutable) {
            throw new IllegalStateException("Action is immutable");
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
     * Returns this.
     */
    @Override
    public DSAction copy() {
        return this;
    }

    /**
     * Defaults to the equals method.
     */
    @Override
    public boolean equals(Object obj) {
        return equals(obj);
    }

    /**
     * Possibly null.
     */
    public String getActionGroup() {
        return actionGroup;
    }

    /**
     * Display name in the group menu, usually null.
     */
    public String getActionGroupDisplay() {
        return actionGroupDisplay;
    }

    @Override
    public int getColumnCount() {
        if (columns != null) {
            return columns.size();
        }
        return 0;
    }

    @Override
    public void getColumnMetadata(int idx, DSMap bucket) {
        bucket.putAll((columns.get(idx)));
    }

    @Override
    public void getMetadata(DSMap bucket) {
        if (actionGroup != null) {
            bucket.put(DSMetadata.ACTION_GROUP, actionGroup);
            if (actionGroupDisplay != null) {
                bucket.put(DSMetadata.ACTION_GROUP_DISPLAY, actionGroupDisplay);
            }
        }
    }

    @Override
    public int getParameterCount() {
        if (parameters != null) {
            return parameters.size();
        }
        return -1;
    }

    /**
     * Puts the metadata from an already added parameter into the bucket, and calls prepareParameter
     * so that is can be updated for any current state.
     *
     * @see #prepareParameter(DSInfo, DSMap)
     */
    @Override
    public void getParameterMetadata(DSInfo<?> target, int idx, DSMap bucket) {
        bucket.putAll(parameters.get(idx));
        prepareParameter(target, bucket);
    }

    @Override
    public ResultsType getResultsType() {
        return result;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Does nothing by default.
     */
    @Override
    public ActionResults invoke(DSIActionRequest request) {
        return null;
    }

    /**
     * Called for each parameter as it is being sent to the requester in response to a list
     * request. The intent is to update the default value to represent the current state of the
     * target.  Does nothing by default.
     *
     * @param target    The target of the action.
     * @param parameter Map representing a single parameter.
     */
    public void prepareParameter(DSInfo<?> target, DSMap parameter) {
    }

    /**
     * Sets the action group, which is null by default.
     */
    public DSAction setActionGroup(String name) {
        if (immutable) {
            throw new IllegalStateException("Action is immutable");
        }
        this.actionGroup = name;
        return this;
    }

    /**
     * Display name in the group menu, usually null.  Not used if the group is null.
     */
    public DSAction setActionGroupDisplay(String name) {
        if (immutable) {
            throw new IllegalStateException("Action is immutable");
        }
        this.actionGroupDisplay = name;
        return this;
    }

    /**
     * Prevents further modification of parameters or return type.  Will throw an
     * exception if already immutable.
     *
     * @throws IllegalStateException If already immutable.
     */
    public DSAction setImmutable(boolean arg) {
        if (immutable) {
            throw new IllegalStateException("Action is immutable");
        }
        this.immutable = arg;
        return this;
    }

    /**
     * Returns this, it is not necessary to set the result to void.
     */
    public DSAction setResultsType(ResultsType result) {
        if (immutable) {
            throw new IllegalStateException("Action is immutable");
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
        String name = params.getString(DSMetadata.NAME);
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
            if (name.equals(param.getString(DSMetadata.NAME))) {
                throw new IllegalArgumentException("Duplicate name: " + name);
            }
        }
    }

}
