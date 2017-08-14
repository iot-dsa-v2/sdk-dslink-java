package org.iot.dsa.io;

import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;

/**
 * Basic implementation of DSReader.  Subclasses must implement the next() method.
 *
 * @author Aaron Hansen
 * @see #next()
 * @see DSReader
 */
public abstract class AbstractReader implements DSReader {

    // Fields
    // ---------

    private Token last = Token.ROOT;
    protected boolean valBoolean;
    protected double valReal;
    protected long valLong;
    protected String valString;

    // Public Methods
    // --------------

    @Override
    public boolean getBoolean() {
        if (last != Token.BOOLEAN) {
            throw new IllegalStateException("Not a boolean");
        }
        return valBoolean;
    }

    @Override
    public double getDouble() {
        switch (last) {
            case DOUBLE:
                break;
            case LONG:
                return (double) valLong;
            default:
                throw new IllegalStateException("Not a double");
        }
        return valReal;
    }

    @Override
    public DSElement getElement() {
        if (last == Token.ROOT) {
            next();
        }
        switch (last) {
            case KEY:
                return DSElement.make(valString);
            case BOOLEAN:
                return DSElement.make(valBoolean);
            case DOUBLE:
                return DSElement.make(valReal);
            case LONG:
                return DSElement.make(valLong);
            case BEGIN_LIST:
                return getList();
            case BEGIN_MAP:
                return getMap();
            case NULL:
                return DSElement.makeNull();
            case STRING:
                return DSElement.make(valString);
        }
        throw new IllegalStateException("Not a value");
    }

   @Override
    public DSList getList() {
        if (last == Token.ROOT) {
            next();
        }
        if (last != Token.BEGIN_LIST) {
            throw new IllegalStateException("Not a list");
        }
        DSList ret = new DSList();
        while (true) {
            switch (next()) {
                case END_INPUT:
                    throw new IllegalStateException("Unexpected end of input");
                case END_LIST:
                    return ret;
                case END_MAP:
                    throw new IllegalStateException("Unexpected end of map in list");
                case KEY:
                    throw new IllegalStateException("Unexpected key in list");
                case BOOLEAN:
                    ret.add(valBoolean);
                    break;
                case DOUBLE:
                    ret.add(valReal);
                    break;
                case LONG:
                    ret.add(valLong);
                    break;
                case BEGIN_LIST:
                    ret.add(getList());
                    break;
                case BEGIN_MAP:
                    ret.add(getMap());
                    break;
                case NULL:
                    ret.addNull();
                    break;
                case STRING:
                    ret.add(valString);
                    break;
                default:
                    throw new IllegalStateException(
                            "Unexpected token in list: " + last);
            }
        }
    }

    @Override
    public long getLong() {
        switch (last) {
            case DOUBLE:
                return (int) valReal;
            case LONG:
                break;
            default:
                throw new IllegalStateException("Not a long");
        }
        return valLong;
    }

    @Override
    public DSMap getMap() {
        if (last == Token.ROOT) {
            next();
        }
        if (last != Token.BEGIN_MAP) {
            throw new IllegalStateException("Not a map");
        }
        DSMap ret = new DSMap();
        String key = null;
        while (true) {
            switch (next()) {
                case KEY:
                    key = valString;
                    break;
                case END_MAP:
                case END_INPUT:
                    return ret;
                default:
                    throw new IllegalStateException("Expecting a key or map end");
            }
            switch (next()) {
                case END_INPUT:
                    throw new IllegalStateException("Unexpected end of input");
                case END_LIST:
                    throw new IllegalStateException("Unexpected end of list in map");
                case END_MAP:
                    return ret;
                case KEY:
                    throw new IllegalStateException("Unexpected key in map");
                case BOOLEAN:
                    ret.put(key, DSElement.make(valBoolean));
                    break;
                case DOUBLE:
                    ret.put(key, DSElement.make(valReal));
                    break;
                case LONG:
                    ret.put(key, DSElement.make(valLong));
                    break;
                case BEGIN_LIST:
                    ret.put(key, getList());
                    break;
                case BEGIN_MAP:
                    ret.put(key, getMap());
                    break;
                case NULL:
                    ret.putNull(key);
                    break;
                case STRING:
                    ret.put(key, DSElement.make(valString));
                    break;
                default:
                    throw new IllegalStateException("Unexpected token in map: " + last);
            }
        }
    }

    @Override
    public String getString() {
        if ((last != Token.STRING) && (last != Token.KEY)) {
            throw new IllegalStateException("Not a string");
        }
        return valString;
    }

    @Override
    public Token last() {
        return last;
    }

    /**
     * Subclasses must override this, read the next item from the stream, then call one of the
     * setXxx methods.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public abstract Token next();

    protected Token setBeginList() {
        return last = Token.BEGIN_LIST;
    }

    protected Token setBeginMap() {
        return last = Token.BEGIN_MAP;
    }

    protected Token setEndList() {
        return last = Token.END_LIST;
    }

    protected Token setEndMap() {
        return last = Token.END_MAP;
    }

    protected Token setEndInput() {
        return last = Token.END_INPUT;
    }

    /**
     * Call setNextValue(String) before calling this.
     *
     * @see #setNextValue(String)
     */
    protected Token setNextKey() {
        return last = Token.KEY;
    }

    protected Token setNextValue(boolean arg) {
        valBoolean = arg;
        return last = Token.BOOLEAN;
    }

    protected Token setNextValue(double arg) {
        valReal = arg;
        return last = Token.DOUBLE;
    }

    /**
     * If this is a key, call this immediately followed by setNextKey()
     *
     * @see #setNextKey()
     */
    protected Token setNextValue(long arg) {
        valLong = arg;
        return last = Token.LONG;
    }

    protected Token setNextValue(String arg) {
        if (arg == null) {
            return setNextValueNull();
        }
        valString = arg;
        return last = Token.STRING;
    }

    protected Token setNextValueNull() {
        return last = Token.NULL;
    }

    @Override
    public AbstractReader reset() {
        last = Token.ROOT;
        return this;
    }


}
