package org.iot.dsa.io;

import java.util.HashMap;
import org.iot.dsa.io.DSReader.Token;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSIObject;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.DSRegistry;
import org.iot.dsa.util.DSException;

/**
 * Decodes a node (tree) that was encoded with NodeEncoder.
 *
 * <p>
 *
 * This is for storing the configuration database, not for DSA interop.
 *
 * @author Aaron Hansen
 * @see NodeEncoder
 */
public class NodeDecoder {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSReader in;
    private HashMap<String, Class> tokenMap = new HashMap<String, Class>();

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    NodeDecoder(DSReader in) {
        this.in = in;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods in alphabetical order
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Reads a node tree from the given input.
     */
    public static DSNode decode(DSReader in) {
        NodeDecoder decoder = new NodeDecoder(in);
        return decoder.read();
    }

    /**
     * Will create new instances for DSNodes, or return the NULL instance for DSIValues.
     */
    private DSIObject getInstance(String type) {
        DSIObject ret = null;
        try {
            Class clazz = null;
            int idx = type.indexOf('=');
            if (idx > 0) {
                String token = type.substring(0, idx);
                String name = type.substring(++idx);
                clazz = Class.forName(name);
                tokenMap.put(token, clazz);
            } else {
                clazz = tokenMap.get(type);
            }
            ret = DSRegistry.getDecoder(clazz);
            if (ret == null) {
                ret = (DSIObject) clazz.newInstance();
            }
        } catch (Exception x) {
            DSException.throwRuntime(x);
        }
        return ret;
    }

    private DSNode read() {
        validateEqual(in.next(), Token.BEGIN_MAP);
        DSNode ret = null;
        while (in.next() != Token.END_MAP) {
            validateEqual(in.last(), Token.KEY);
            String key = in.getString();
            if ("t".equals(key)) {
                validateEqual(in.next(), Token.STRING);
                ret = (DSNode) getInstance(in.getString());
            } else if ("n".equals(key)) {
                validateEqual(in.next(), Token.STRING);
            } else if ("i".equals(key)) {
                in.next(); //just skip
            } else if ("v".equals(key)) {
                if (ret == null) {
                    throw new IllegalStateException("Type not read before children");
                }
                readChildren(ret);
            }
        }
        return ret;
    }

    private void readChild(DSNode parent) {
        String name = null;
        DSElement state = null;
        String type = null;
        DSInfo info = null;
        while (in.next() != Token.END_MAP) {
            validateEqual(in.last(), Token.KEY);
            String key = in.getString();
            if ("t".equals(key)) {
                validateEqual(in.next(), Token.STRING);
                type = in.getString();
            } else if ("n".equals(key)) {
                validateEqual(in.next(), Token.STRING);
                name = in.getString();
                info = parent.getInfo(name);
                if (info != null) {
                    ;//TODO parent.reorderToLast(info);
                }
            } else if ("i".equals(key)) {
                in.next();
                state = in.getElement();
            } else if ("v".equals(key)) {
                if (name == null) {
                    throw new IllegalStateException("Missing name");
                }
                DSIObject obj = null;
                if (type != null) {
                    obj = getInstance(type);
                    parent.put(name, obj);
                    info = parent.getInfo(name);
                } else {
                    if (info == null) {
                        throw new IllegalStateException("Missing type for a dynamic child");
                    }
                    obj = info.getObject();
                }
                if (state != null) {
                    info.decodeState(state);
                }
                if (obj instanceof DSNode) {
                    readChildren((DSNode) obj);
                } else {
                    in.next();
                    DSIValue val = (DSIValue) obj;
                    parent.put(info, val.decode(in.getElement()));
                }
            }
        }
    }

    private void readChildren(DSNode parent) {
        validateEqual(in.next(), Token.BEGIN_LIST);
        while (true) {
            DSReader.Token token = in.next();
            switch (token) {
                case BEGIN_MAP:
                    readChild(parent);
                    break;
                case END_LIST:
                    return;
                case END_INPUT:
                    throw new IllegalStateException("Unexpected end of input");
            }
        }
    }

    private void validateEqual(Token next, Token desired) {
        if (next != desired) {
            throw new IllegalStateException("Expecting " + desired + ", but got " + next);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Initialization
    ///////////////////////////////////////////////////////////////////////////

}
