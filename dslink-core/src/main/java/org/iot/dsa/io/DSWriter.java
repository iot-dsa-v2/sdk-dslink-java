package org.iot.dsa.io;

import java.io.Closeable;
import org.iot.dsa.node.DSElement;

/**
 * An encoder that can be used to encode large graphs with or without object instances.
 *
 * <p>
 *
 * To simply encode a DSMap or DSList, use the value(DSElement) method.
 *
 * For example:
 *
 * <ul>
 *
 * <li>new JsonWriter(out).value(myMap).close();
 *
 * </ul>
 *
 * <p>
 *
 * Otherwise, you can stream data struct without using any DSIObject instances:
 *
 * <ul>
 *
 * <li>out.newMap().key("a").value(1).key("b").value(2).key("c").value(3).endMap();
 *
 * <p>
 *
 * Be aware that if the underlying encoding (such as JSON) doesn't provide a mechanism to
 * differentiate between data types (such as numbers), values might not get as the same type
 * they were encoded.
 *
 * @author Aaron Hansen
 */
public interface DSWriter extends Closeable {

    // Public Methods
    // --------------

    /**
     * Start a new list and return this.
     *
     * @throws IllegalStateException when improperly called.
     */
    public DSWriter beginList();

    /**
     * Start a new map and return this.
     *
     * @throws IllegalStateException when improperly called.
     */
    public DSWriter beginMap();

    /**
     * Close the stream. IOExceptions will be wrapped in runtime exceptions.
     */
    public void close();

    /**
     * End the current list.
     *
     * @throws IllegalStateException when improperly called.
     */
    public DSWriter endList();

    /**
     * End the current map.
     *
     * @throws IllegalStateException when improperly called.
     */
    public DSWriter endMap();

    /**
     * Flush the stream. IOExceptions will be wrapped in runtime exceptions.
     */
    public DSWriter flush();

    /**
     * Write a key in the current map.  Cannot be called in a list, must be followed by a call to
     * one of the value methods.
     *
     * @throws IllegalStateException when improperly called.
     */
    public DSWriter key(CharSequence key);

    /**
     * Clears the state of the writer.
     */
    public DSWriter reset();

    /**
     * Write a value to the map or list.  If in a map, this must have been preceded by a call to
     * key(String).  This can be used to encode an entire graph.
     *
     * @throws IllegalStateException when improperly called.
     */
    public DSWriter value(DSElement arg);

    /**
     * Write a value to the map or list.  If in a map, this must have been preceded by a call to
     * key(String).
     *
     * @throws IllegalStateException when improperly called.
     */
    public DSWriter value(boolean arg);

    /**
     * Write a value to the map or list.  If in a map, this must have been preceded by a call to
     * key(String).
     *
     * @throws IllegalStateException when improperly called.
     */
    public DSWriter value(double arg);

    /**
     * Write a value to the map or list.  If in a map, this must have been preceded by a call to
     * key(String).
     *
     * @throws IllegalStateException when improperly called.
     */
    public DSWriter value(int arg);

    /**
     * Write a value to the map or list.  If in a map, this must have been preceded by a call to
     * key(String).
     *
     * @throws IllegalStateException when improperly called.
     */
    public DSWriter value(long arg);

    /**
     * Write a value to the map or list.  If in a map, this must have been preceded by a call to
     * key(String).
     *
     * @throws IllegalStateException when improperly called.
     */
    public DSWriter value(String arg);


}
