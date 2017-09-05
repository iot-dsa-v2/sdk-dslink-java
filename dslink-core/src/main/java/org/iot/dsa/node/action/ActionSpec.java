package org.iot.dsa.node.action;

import java.util.Iterator;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.security.DSPermission;

/**
 * Defines an invokable node in the DSA model.
 *
 * @author Aaron Hansen
 */
public interface ActionSpec {

    /**
     * Returns an iterator for each parameter.
     *
     * @return Null if there are no parameters.
     */
    public Iterator<DSMap> getParameters();

    /**
     * Minimum permission level required to invoke.
     *
     * @return Never null.
     */
    public DSPermission getPermission();

    /**
     * What the action returns.
     */
    public ResultType getResultType();

    /**
     * This is only called for the VALUES result type.
     */
    public Iterator<DSMap> getValueResults();

    /**
     * Defines what the action returns.
     */
    public enum ResultType {

        /**
         * A finite sized table whose stream is closed when the row iterator is complete.
         */
        CLOSED_TABLE("table"),

        /**
         * A finite sized table whose stream is left open after the row iterator is complete because
         * it can change over time.  The row iterator does not have to represent the entire table.
         */
        OPEN_TABLE("stream"),

        /**
         * A stream of rows.  Clients can choose to trim rows for memory management.
         */
        STREAM_TABLE("stream"),

        /**
         * No return value.
         */
        VOID(""),

        /**
         * One or more return values.
         */
        VALUES("values");

        private String display;

        ResultType(String display) {
            this.display = display;
        }

        /**
         * True if CLOSED_TABLE, VOID, or VALUES.
         */
        public boolean isClosed() {
            return (this == CLOSED_TABLE) || (this == VOID) || (this == VALUES);
        }

        /**
         * True if OPEN_TABLE or STREAM_TABLE
         */
        public boolean isOpen() {
            return (this == OPEN_TABLE) || (this == STREAM_TABLE);
        }

        /**
         * True if this is the STREAM_TABLE.
         */
        public boolean isStream() {
            return this == STREAM_TABLE;
        }

        public boolean isVoid() {
            return this == VOID;
        }

        public boolean isValues() {
            return this == VALUES;
        }

        public String toString() {
            return display;
        }

    } //ResultType

}
