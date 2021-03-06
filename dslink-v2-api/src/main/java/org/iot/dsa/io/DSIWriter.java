package org.iot.dsa.io;

import java.io.Closeable;
import org.iot.dsa.node.DSElement;

/**
 * An encoder that can be used to encode large graphs with or without object instances.
 * <p>
 * To simply encode a DSMap or DSList, use the value(DSElement) method.
 * <p>
 * For example:
 * <ul>
 * <li>new JsonWriter(out).value(myMap).close();
 * </ul>
 * <p>
 * Otherwise, you can stream data struct without using any DSIObject instances:
 * <ul>
 * <li>out.newMap().key("a").value(1).key("b").value(2).key("c").value(3).endMap();
 * </ul>
 * <p>
 * Be aware that if the underlying encoding (such as JSON) doesn't provide a mechanism to
 * differentiate between data types (such as numbers), values might not get as the same type they
 * were encoded.
 *
 * @author Aaron Hansen
 */
public interface DSIWriter extends Closeable {

    // Public Methods
    // --------------

    /**
     * Start a new list and return this.
     *
     * @throws IllegalStateException when improperly called.
     */
    DSIWriter beginList();

    /**
     * Start a new list of the given size and return this.
     *
     * @param size Less than zero means the size is unknown.
     * @throws IllegalStateException when improperly called.
     */
    DSIWriter beginList(int size);

    /**
     * Start a new map and return this.
     *
     * @throws IllegalStateException when improperly called.
     */
    DSIWriter beginMap();

    /**
     * Start a new map of the given size and return this.
     *
     * @param size Less than zero means the size is unknown.
     * @throws IllegalStateException when improperly called.
     */
    DSIWriter beginMap(int size);

    /**
     * Close the stream. IOExceptions will be wrapped in runtime exceptions.
     */
    @Override
    void close();

    /**
     * End the current list.
     *
     * @throws IllegalStateException when improperly called.
     */
    DSIWriter endList();

    /**
     * End the current map.
     *
     * @throws IllegalStateException when improperly called.
     */
    DSIWriter endMap();

    /**
     * Flush the stream. IOExceptions will be wrapped in runtime exceptions.
     */
    DSIWriter flush();

    /**
     * Write a key in the current map.  Cannot be called in a list, must be followed by a call to
     * one of the value methods.
     *
     * @throws IllegalStateException when improperly called.
     */
    DSIWriter key(CharSequence key);

    /**
     * If the writer is buffering output, this returns the size of that buffer.
     */
    int length();

    /**
     * Clears the state of the writer.
     */
    DSIWriter reset();

    /**
     * Write a value to the map or list.  If in a map, this must have been preceded by a call to
     * key(String).  This can be used to encode an entire graph.
     *
     * @throws IllegalStateException when improperly called.
     */
    DSIWriter value(DSElement arg);

    /**
     * Write a value to the map or list.  If in a map, this must have been preceded by a call to
     * key(String).
     *
     * @throws IllegalStateException when improperly called.
     */
    DSIWriter value(boolean arg);

    /**
     * Write a value to the map or list.  If in a map, this must have been preceded by a call to
     * key(String).
     *
     * @throws IllegalStateException when improperly called.
     */
    DSIWriter value(byte[] arg);

    /**
     * Write a value to the map or list.  If in a map, this must have been preceded by a call to
     * key(String).
     *
     * @throws IllegalStateException when improperly called.
     */
    DSIWriter value(double arg);

    /**
     * Write a value to the map or list.  If in a map, this must have been preceded by a call to
     * key(String).
     *
     * @throws IllegalStateException when improperly called.
     */
    DSIWriter value(int arg);

    /**
     * Write a value to the map or list.  If in a map, this must have been preceded by a call to
     * key(String).
     *
     * @throws IllegalStateException when improperly called.
     */
    DSIWriter value(long arg);

    /**
     * Write a value to the map or list.  If in a map, this must have been preceded by a call to
     * key(String).
     *
     * @throws IllegalStateException when improperly called.
     */
    DSIWriter value(String arg);


}
