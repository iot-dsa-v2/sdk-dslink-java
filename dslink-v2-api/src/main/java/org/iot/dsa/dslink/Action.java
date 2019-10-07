package org.iot.dsa.dslink;

import org.iot.dsa.node.DSMap;

/**
 * Defines an invokable node in the DSA model.
 *
 * @author Aaron Hansen
 */
public interface Action extends Node {

    /**
     * Only needed for a VALUES type actions, and optional even then.
     */
    public default int getColumnCount() {
        return 0;
    }

    /**
     * Add the metadata for the column at the given index to the bucket.  Only called before the
     * action invocation.  Throws an IllegalStateException by default.
     */
    public default void getColumnMetadata(int idx, DSMap bucket) {
        throw new IllegalStateException("Method not overridden");
    }

    /**
     * Return 0 or less if there are no parameters.  Returns 0 by default.
     */
    public default int getParameterCount() {
        return 0;
    }

    /**
     * Add the metadata for the parameter at the given index to the bucket.  Throws an
     * IllegalStateException by default.
     */
    public default void getParameterMetadata(int idx, DSMap bucket) {
        throw new IllegalStateException("Method not overridden");
    }

    /**
     * What the action returns, returns VOID by default.
     */
    public default ResultsType getResultsType() {
        return ResultsType.VOID;
    }

    /**
     * Whether or not the action the action requires write permission.  Returns false by default.
     */
    public default boolean isReadOnly() {
        return false;
    }

    /**
     * Defines what the action returns.
     */
    public enum ResultsType {

        /**
         * A stream of rows.  Clients can choose to trim rows for memory management.
         */
        STREAM("stream"),

        /**
         * A finite sized table whose stream is closed when the row cursor is complete.
         */
        TABLE("table"),

        /**
         * A single row of values.
         */
        VALUES("values"),

        /**
         * No return value.
         */
        VOID("");

        private String display;

        ResultsType(String display) {
            this.display = display;
        }

        public boolean isStream() {
            return this == STREAM;
        }

        public boolean isTable() {
            return this == TABLE;
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
