package org.iot.dsa.node.action;

import java.util.ArrayList;
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
 * Actions allow you to expose functionality much more complex than simple reads and writes.
 *
 * @author Aaron Hansen
 */
public abstract class DSAction implements ActionSpec, DSIMetadata, DSIObject {

    ///////////////////////////////////////////////////////////////////////////
    // Class Fields
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Simple action stub that does nothing.  Intended for use by overriding DSNode.invoke
     * and handling action there.
     */
    public static final DSAction.Noop DEFAULT = new Noop();

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
    private ResultType result = ResultType.VOID;

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
            columns = new ArrayList<DSMap>();
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
        if (value instanceof DSIMetadata) {
            ((DSIMetadata) value).getMetadata(ret.getMap());
        }
        ret.setName(name)
           .setType(value);
        addColumnMetadata(ret.getMap());
        return ret;
    }

    /**
     * Creates a DSMetadata, calls setName and setType on it, adds the internal map to
     * the results list and returns the metadata instance for further configuration.
     *
     * @param name Must not be null.
     * @param type Must not be null.
     * @return Metadata for further configuration.
     */
    public DSMetadata addColumnMetadata(String name, DSValueType type) {
        if (immutable) {
            throw new IllegalStateException("Action is immutable");
        }
        DSMetadata ret = new DSMetadata();
        ret.setName(name)
           .setType(type);
        addColumnMetadata(ret.getMap());
        return ret;
    }

    /**
     * A convenience which calls addParameter with the same arguments, and also sets the
     * metadata with the given value as the default.
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
     * the parameter list and returns the metadata instance for further configuration.
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
        ret.setName(name)
           .setType(value)
           .setDefault(value)
           .setDescription(description);
        addParameter(ret.getMap());
        return ret;
    }

    /**
     * Creates a DSMetadata, calls setName and setType on it, adds the internal map to
     * the parameter list and returns the metadata instance for further configuration.
     *
     * @param name        Must not be null.
     * @param type        Must not be null.
     * @param description Can be null.
     * @return Metadata for further configuration.
     */
    public DSMetadata addParameter(String name, DSValueType type, String description) {
        if (immutable) {
            throw new IllegalStateException("Action is immutable");
        }
        DSMetadata ret = new DSMetadata();
        ret.setName(name)
           .setType(type)
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
        return -1;
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

    @Override
    public void getParameterMetadata(int idx, DSMap bucket) {
        bucket.putAll(parameters.get(idx));
    }

    /**
     * Not used.  Permissions are determined using info flags.  If the admin flag is set,
     * the action requires admin level permissions. If the action is readonly, then only
     * read permissions are required.  Otherwise the action will require write permissions.
     */
    @Override
    public DSPermission getPermission() {
        return DSPermission.WRITE;
    }

    @Override
    public ResultType getResultType() {
        return result;
    }

    /**
     * Execute the action invocation for the given target.  It is safe to use the calling thread
     * for long lived operations.  If the return type is void, perform the full operation on the
     * calling thread so that errors will be properly reported.
     * <p>
     * To report an error, simply throw a runtime exception from this method, or call
     * ActionInvocation.close(Exception) when processing asynchronously.
     *
     * @param target  The info about the target of the action (its parent).
     * @param request Details about the incoming invoke as well as the mechanism to
     *                send async updates over an open stream.
     * @return Can be null if the result type is void.
     * @throws RuntimeException Throw a runtime exception to report an error and close the stream.
     */
    public abstract ActionResult invoke(DSInfo target, ActionInvocation request);

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
     * Called for each parameter as it is being sent to the requester. The intent is for
     * updating the default value to represent the current state of the target.  If you do not
     * need to prepare parameters, the inner class Parameterless implements this method to
     * do nothing.
     *
     * @param target    The info about the target of the action (its parent).
     * @param parameter Map representing a single parameter.
     */
    public abstract void prepareParameter(DSInfo target, DSMap parameter);

    /**
     * Sets the action group, which is null by default.
     */
    public DSAction setActionGroup(String name) {
        this.actionGroup = name;
        return this;
    }

    /**
     * Display name in the group menu, usually null.  Not used if the group is null.
     */
    public DSAction setActionGroupDisplay(String name) {
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
    public DSAction setResultType(ResultType result) {
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

    /**
     * Implements invoke to simply return null.
     */
    public static class Noop extends Parameterless {

        @Override
        public ActionResult invoke(DSInfo target, ActionInvocation invocation) {
            return null;
        }
    }

    /**
     * Implements prepareParameter to do nothing.
     */
    public abstract static class Parameterless extends DSAction {

        @Override
        public void prepareParameter(DSInfo info, DSMap parameter) {
        }
    }

}
