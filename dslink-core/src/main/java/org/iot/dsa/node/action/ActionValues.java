package org.iot.dsa.node.action;

import java.util.Iterator;
import org.iot.dsa.node.DSIValue;

/**
 * Simple set of return values from an action.
 *
 * @author Aaron Hansen
 */
public interface ActionValues extends ActionResult {

    public Iterator<DSIValue> getValues();

}
