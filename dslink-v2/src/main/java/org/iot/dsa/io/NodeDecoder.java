package org.iot.dsa.io;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import org.iot.dsa.io.DSIReader.Token;
import org.iot.dsa.io.json.JsonReader;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSIObject;
import org.iot.dsa.node.DSIStorable;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.DSRegistry;
import org.iot.dsa.util.DSException;

/**
 * Decodes a node (tree) that was encoded with NodeEncoder.
 * <p>
 * <p>
 * <p>
 * This is for storing the configuration database, not for DSA interop.
 *
 * @author Aaron Hansen
 * @see NodeEncoder
 */
public class NodeDecoder {

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSIReader in;
    private HashMap<String, Class> tokenMap = new HashMap<String, Class>();

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    NodeDecoder(DSIReader in) {
        this.in = in;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods in alphabetical order
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Reads a node tree from the given input.
     */
    public static DSNode decode(DSIReader in) {
        NodeDecoder decoder = new NodeDecoder(in);
        return decoder.read();
    }

    /**
     * Reads a node tree from the give byte array.
     */
    public static DSNode decode(byte[] bytes) {
        ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
        JsonReader reader = new JsonReader(bin, "UTF-8");
        DSNode ret = decode(reader);
        reader.close();
        return ret;
    }

    /**
     * Will create new instances for anything without a decoder, otherwise returns the decoder
     * instance.
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
            validateEqual(in.last(), Token.STRING);
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
        DSMap meta = null;
        DSIObject obj = null;
        while (in.next() != Token.END_MAP) {
            validateEqual(in.last(), Token.STRING);
            String key = in.getString();
            if ("t".equals(key)) {
                validateEqual(in.next(), Token.STRING);
                type = in.getString();
            } else if ("n".equals(key)) {
                validateEqual(in.next(), Token.STRING);
                name = in.getString();
                info = parent.getInfo(name);
            } else if ("i".equals(key)) {
                in.next();
                state = in.getElement();
            } else if ("m".equals(key)) {
                validateEqual(in.next(), Token.BEGIN_MAP);
                meta = in.getMap();
            } else if ("v".equals(key)) {
                if (name == null) {
                    throw new IllegalStateException("Missing name");
                }
                if (type != null) {
                    obj = getInstance(type);
                    if (info == null) {
                        info = parent.put(name, obj);
                    } else {
                        parent.put(info, obj);
                    }
                } else if (info != null) {
                    obj = info.getObject();
                }
                if (obj == null) { //dynamic, or declareDefaults was modified
                    in.next();
                    obj = in.getElement();
                    info = parent.put(name, obj);
                } else if (obj instanceof DSNode) {
                    readChildren((DSNode) obj);
                } else {
                    in.next();
                    DSIValue val = (DSIValue) obj;
                    if (val instanceof DSIStorable) {
                        parent.put(info, ((DSIStorable) val).restore(in.getElement()));
                    } else {
                        parent.put(info, val.valueOf(in.getElement()));
                    }
                }
            }
        }
        if (obj == null) { //Node with no children
            if (name == null) {
                throw new IllegalStateException("Missing name");
            }
            if (type != null) {
                obj = getInstance(type);
                if (info == null) {
                    info = parent.put(name, obj);
                } else {
                    parent.put(info, obj);
                }
            } else if (info != null) {
                obj = info.getObject();
            }
        }
        if (info != null) {
            if (state != null) {
                info.decodeState(state);
            }
            if (meta != null) {
                info.setMetadata(meta);
            }
        }
        if (obj == null) {
            throw new IllegalStateException("Could not decode " + parent.getPath() + "/" + name);
        }
        //TODO parent.reorderToLast(info);
    }

    private void readChildren(DSNode parent) {
        validateEqual(in.next(), Token.BEGIN_LIST);
        while (true) {
            DSIReader.Token token = in.next();
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
