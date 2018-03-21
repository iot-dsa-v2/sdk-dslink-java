package org.iot.dsa.node.action;

import java.util.ArrayList;
import java.util.Iterator;
import org.iot.dsa.node.DSIValue;

/**
 * This is a convenience implementation of ActionValues.  It is for actions that return one or
 * more values (not tables or streams).
 *
 * @author Aaron Hansen
 */
public class DSActionValues implements ActionValues {

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSAction action;
    private ArrayList<DSIValue> values = new ArrayList<DSIValue>();

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public DSActionValues(DSAction action) {
        this.action = action;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods in alphabetical order
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Values must be added in the same order they were defined in the action.
     */
    public DSActionValues addResult(DSIValue value) {
        values.add(value);
        return this;
    }

    public DSAction getAction() {
        return action;
    }

    public Iterator<DSIValue> getValues() {
        return values.iterator();
    }

    /**
     * Does nothing.
     */
    public void onClose() {
    }

}
