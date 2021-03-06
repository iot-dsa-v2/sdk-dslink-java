package org.iot.dsa.io;

import java.io.Closeable;
import java.io.IOException;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSMap.Entry;

/**
 * Basic implementation of DSWriter.  Subclasses must implement the abstract methods which all start
 * with write.
 *
 * @author Aaron Hansen
 * @see DSIWriter
 */
public abstract class AbstractWriter implements Closeable, DSIWriter {

    // Constants
    // ---------

    private static final int LAST_DONE = 0; //document complete
    private static final int LAST_END = 1;  //end of map/list
    private static final int LAST_INIT = 2; //start
    private static final int LAST_KEY = 3;  //object key
    private static final int LAST_LIST = 4; //started a list
    private static final int LAST_MAP = 5;  //started a map
    private static final int LAST_VAL = 6;  //list or object value

    // Fields
    // ------

    private int depth = 0;
    private int last = LAST_INIT;

    /**
     * Subclasses can use this if applicable.
     */
    protected boolean prettyPrint = false;

    // Constructors
    // ------------

    // Public Methods
    // --------------

    @Override
    public AbstractWriter beginList() {
        return beginList(-1);
    }

    @Override
    public AbstractWriter beginList(int size) {
        try {
            switch (last) {
                case LAST_MAP:
                    throw new IllegalStateException("Expecting map key.");
                case LAST_DONE:
                    throw new IllegalStateException("Nesting error.");
                case LAST_VAL:
                case LAST_END:
                    writeSeparator();
                default:
                    if (prettyPrint && (last != LAST_INIT) && (last != LAST_KEY)) {
                        writeNewLineIndent();
                    }
            }
            writeListStart(size);
            last = LAST_LIST;
            depth++;
        } catch (IOException x) {
            throw new RuntimeException(x);
        }
        return this;
    }

    @Override
    public AbstractWriter beginMap() {
        return beginMap(-1);
    }

    @Override
    public AbstractWriter beginMap(int size) {
        try {
            switch (last) {
                case LAST_MAP:
                    throw new IllegalStateException("Expecting map key.");
                case LAST_DONE:
                    throw new IllegalStateException("Nesting error.");
                case LAST_VAL:
                case LAST_END:
                    writeSeparator();
                default:
                    if (prettyPrint && (last != LAST_INIT) && (last != LAST_KEY)) {
                        writeNewLineIndent();
                    }
            }
            writeMapStart(size);
            last = LAST_MAP;
            depth++;
        } catch (IOException x) {
            throw new RuntimeException(x);
        }
        return this;
    }

    @Override
    public AbstractWriter endList() {
        try {
            if (depth == 0) {
                throw new IllegalStateException("Nesting error.");
            }
            depth--;
            if (prettyPrint) {
                writeNewLineIndent();
            }
            writeListEnd();
            if (depth == 0) {
                last = LAST_DONE;
            } else {
                last = LAST_END;
            }
        } catch (IOException x) {
            throw new RuntimeException(x);
        }
        return this;
    }

    @Override
    public AbstractWriter endMap() {
        try {
            if (depth == 0) {
                throw new IllegalStateException("Nesting error.");
            }
            depth--;
            if (prettyPrint) {
                writeNewLineIndent();
            }
            writeMapEnd();
            if (depth == 0) {
                last = LAST_DONE;
            } else {
                last = LAST_END;
            }
        } catch (IOException x) {
            throw new RuntimeException(x);
        }
        return this;
    }

    @Override
    public AbstractWriter key(CharSequence arg) {
        try {
            switch (last) {
                case LAST_DONE:
                    throw new IllegalStateException("Nesting error: " + arg);
                case LAST_INIT:
                    throw new IllegalStateException("Not expecting: " + arg + " (" + last + ")");
                case LAST_VAL:
                case LAST_END:
                    writeSeparator();
                default:
                    if (prettyPrint) {
                        writeNewLineIndent();
                    }
            }
            writeKey(arg);
            last = LAST_KEY;
            writeKeyValueSeparator();
        } catch (IOException x) {
            throw new RuntimeException(x);
        }
        return this;
    }

    /**
     * Returns 0 by default.
     */
    @Override
    public int length() {
        return 0;
    }

    @Override
    public AbstractWriter reset() {
        depth = 0;
        last = LAST_INIT;
        return this;
    }

    @Override
    public AbstractWriter value(DSElement arg) {
        if (arg == null) {
            return value((String) null);
        }
        switch (arg.getElementType()) {
            case BOOLEAN:
                value(arg.toBoolean());
                break;
            case BYTES:
                value(arg.toBytes());
                break;
            case LONG:
                value(arg.toLong());
                break;
            case LIST:
                DSList list = arg.toList();
                beginList(list.size());
                for (int i = 0, len = list.size(); i < len; i++) {
                    value(list.get(i));
                }
                endList();
                break;
            case MAP:
                DSMap map = arg.toMap();
                beginMap(map.size());
                Entry e = map.firstEntry();
                while (e != null) {
                    key(e.getKey()).value(e.getValue());
                    e = e.next();
                }
                endMap();
                break;
            case NULL:
                value((String) null);
                break;
            case DOUBLE:
                value(arg.toDouble());
                break;
            case STRING:
                value(arg.toString());
                break;
        }
        return this;
    }

    @Override
    public AbstractWriter value(boolean arg) {
        try {
            switch (last) {
                case LAST_DONE:
                    throw new IllegalStateException("Nesting error: " + arg);
                    //case LAST_INIT:
                    //throw new IllegalStateException("Not expecting value: " + arg);
                case LAST_VAL:
                case LAST_END:
                    writeSeparator();
                    if (prettyPrint) {
                        writeNewLineIndent();
                    }
                    break;
                case LAST_LIST:
                    if (prettyPrint) {
                        writeNewLineIndent();
                    }
            }
            write(arg);
            last = LAST_VAL;
        } catch (IOException x) {
            throw new RuntimeException(x);
        }
        return this;
    }

    @Override
    public AbstractWriter value(byte[] arg) {
        try {
            switch (last) {
                case LAST_DONE:
                    throw new IllegalStateException("Nesting error");
                    //case LAST_INIT:
                    //throw new IllegalStateException("Not expecting byte[] value");
                case LAST_VAL:
                case LAST_END:
                    writeSeparator();
                    if (prettyPrint) {
                        writeNewLineIndent();
                    }
                    break;
                case LAST_LIST:
                    if (prettyPrint) {
                        writeNewLineIndent();
                    }
            }
            write(arg);
            last = LAST_VAL;
        } catch (IOException x) {
            throw new RuntimeException(x);
        }
        return this;
    }

    @Override
    public AbstractWriter value(double arg) {
        try {
            switch (last) {
                case LAST_DONE:
                    throw new IllegalStateException("Nesting error: " + arg);
                    //case LAST_INIT:
                    //throw new IllegalStateException("Not expecting value: " + arg);
                case LAST_VAL:
                case LAST_END:
                    writeSeparator();
                    if (prettyPrint) {
                        writeNewLineIndent();
                    }
                    break;
                case LAST_LIST:
                    if (prettyPrint) {
                        writeNewLineIndent();
                    }
            }
            write(arg);
            last = LAST_VAL;
        } catch (IOException x) {
            throw new RuntimeException(x);
        }
        return this;
    }

    @Override
    public AbstractWriter value(int arg) {
        try {
            switch (last) {
                case LAST_DONE:
                    throw new IllegalStateException("Nesting error: " + arg);
                    //case LAST_INIT:
                    //throw new IllegalStateException("Not expecting value: " + arg);
                case LAST_VAL:
                case LAST_END:
                    writeSeparator();
                    if (prettyPrint) {
                        writeNewLineIndent();
                    }
                    break;
                case LAST_LIST:
                    if (prettyPrint) {
                        writeNewLineIndent();
                    }
            }
            write(arg);
            last = LAST_VAL;
        } catch (IOException x) {
            throw new RuntimeException(x);
        }
        return this;
    }

    @Override
    public AbstractWriter value(long arg) {
        try {
            switch (last) {
                case LAST_DONE:
                    throw new IllegalStateException("Nesting error: " + arg);
                    //case LAST_INIT:
                    //throw new IllegalStateException("Not expecting value: " + arg);
                case LAST_VAL:
                case LAST_END:
                    writeSeparator();
                    if (prettyPrint) {
                        writeNewLineIndent();
                    }
                    break;
                case LAST_LIST:
                    if (prettyPrint) {
                        writeNewLineIndent();
                    }
            }
            write(arg);
            last = LAST_VAL;
        } catch (IOException x) {
            throw new RuntimeException(x);
        }
        return this;
    }

    @Override
    public AbstractWriter value(String arg) {
        try {
            switch (last) {
                case LAST_DONE:
                    throw new IllegalStateException("Nesting error: " + arg);
                    //case LAST_INIT:
                    //throw new IllegalStateException("Not expecting value: " + arg);
                case LAST_VAL:
                case LAST_END:
                    writeSeparator();
                    if (prettyPrint) {
                        writeNewLineIndent();
                    }
                    break;
                case LAST_LIST:
                    if (prettyPrint) {
                        writeNewLineIndent();
                    }
            }
            if (arg == null) {
                writeNull();
            } else {
                writeValue(arg);
            }
            last = LAST_VAL;
        } catch (IOException x) {
            throw new RuntimeException(x);
        }
        return this;
    }

    /**
     * Current depth in the tree, will be needed by writeNewLineIndent.
     */
    protected int getDepth() {
        return depth;
    }

    /**
     * Write the value.
     */
    protected abstract void write(boolean arg);

    /**
     * Write the value.
     */
    protected abstract void write(byte[] arg) throws IOException;

    /**
     * Write the value.
     */
    protected abstract void write(double arg) throws IOException;

    /**
     * Write the value.
     */
    protected abstract void write(long arg) throws IOException;

    /**
     * Write string key of a map entry.
     */
    protected abstract void writeKey(CharSequence arg) throws IOException;

    /**
     * Separate the key from the value in a map.
     */
    protected abstract void writeKeyValueSeparator() throws IOException;

    /**
     * End the current list.
     */
    protected abstract void writeListEnd() throws IOException;

    /**
     * Start a new list.
     */
    protected abstract void writeListStart(int size) throws IOException;

    /**
     * End the current map.
     */
    protected abstract void writeMapEnd() throws IOException;

    /**
     * Start a new map.
     */
    protected abstract void writeMapStart(int size) throws IOException;

    /**
     * Override point for subclasses which perform use pretty printing, such as json. Does nothing
     * by default.
     *
     * @see #getDepth()
     */
    protected void writeNewLineIndent() {
    }

    /**
     * Write a null value.
     */
    protected abstract void writeNull();

    /**
     * Write a value separator, such as the comma in json.
     */
    protected abstract void writeSeparator() throws IOException;

    /**
     * Write the value, which will never be null.
     */
    protected abstract void writeValue(CharSequence arg) throws IOException;

}
