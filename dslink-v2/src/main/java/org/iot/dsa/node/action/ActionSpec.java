package org.iot.dsa.node.action;

import org.iot.dsa.node.DSMap;
import org.iot.dsa.security.DSPermission;

/**
 * Defines an invokable node in the DSA model.
 *
 * @author Aaron Hansen
 */
public interface ActionSpec {

    /**
     * Returns 0 or less if the result columns are unknown in advance.
     */
    public int getColumnCount();

    /**
     * Adds the metadata for the column at the given index to the bucket.  Only called before the
     * action invocation.
     */
    public void getColumnMetadata(int idx, DSMap bucket);

    /**
     * Returns 0 or less if there are no parameters.
     */
    public int getParameterCount();

    /**
     * Adds the metadata for the parameter at the given index to the bucket.
     */
    public void getParameterMetadata(int idx, DSMap bucket);

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
     * Defines what the action returns.
     */
    public enum ResultType {

        /**
         * A finite sized table whose stream is closed when the row cursor is complete.
         */
        CLOSED_TABLE("table"),

        /**
         * A finite sized table whose stream is left open after the row iterator is complete because
         * it can change over time.  The initial row cursor does not have to transmit the entire
         * table.
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
         * A single row table.
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

        public boolean isValues() {
            return this == VALUES;
        }

        public boolean isVoid() {
            return this == VOID;
        }

        public String toString() {
            return display;
        }

    } //ResultType

}
