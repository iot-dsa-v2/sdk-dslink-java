package org.iot.dsa.io;

import java.util.HashMap;
import org.iot.dsa.io.json.AbstractJsonWriter;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSIObject;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSNode;

/**
 * Encodes a node tree using a compact JSON schema.  Defaults are omitted and class names are
 * tokenized to minimize size. Use NodeDecoder for deserialization.
 *
 * <p>
 *
 * This is for saving a configuration database, not for DSA interop.
 *
 * @author Aaron Hansen
 * @see NodeDecoder
 */
public class NodeEncoder {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private int nextToken = 1;
    private DSWriter out;
    private HashMap<Class, String> classMap = new HashMap<Class, String>();

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    NodeEncoder(DSWriter out) {
        this.out = out;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods in alphabetical order
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Writes the node tree to the given writer.  Flushes but does not close the writer.
     *
     * @param out  Where to write the node, also the return value (unclosed).
     * @param node What to encode.
     * @return The writer parameter, flushed, but not closed.
     */
    public static DSWriter encode(DSWriter out, DSNode node) {
        NodeEncoder encoder = new NodeEncoder(out);
        encoder.write(node);
        out.flush();
        return out;
    }

    private String getToken(Object arg) {
        Class clazz = arg.getClass();
        String ret = classMap.get(clazz);
        if (ret == null) {
            ret = "t" + nextToken;
            nextToken++;
            classMap.put(clazz, ret);
            ret = ret + '=' + clazz.getName();
        }
        return ret;
    }

    void write(DSNode arg) {
        DSInfo info = arg.getInfo();
        if (info != null) {
            writeNode(info);
            return;
        }
        out.beginMap();
        out.key("t").value(getToken(arg));
        writeChildren(arg);
        out.endMap();
    }

    void writeDefault(DSInfo arg) {
        out.beginMap();
        if (out instanceof AbstractJsonWriter) {
            ((AbstractJsonWriter) out).writeNewLineIndent();
        }
        out.key("n").value(arg.getName());
        out.endMap();
    }

    void writeChildren(DSNode arg) {
        int len = arg.childCount();
        if (len == 0) {
            return;
        }
        DSIObject obj;
        DSInfo info;
        out.key("v");
        if (arg == null) {
            out.value((String) null);
            return;
        }
        out.beginList();
        for (int i = 0; i < len; i++) {
            try {
                info = arg.getInfo(i);
                obj = info.getObject();
                if (info.isTransient()) {
                    //skip it
                } else if (info.equalsDefault()) {  //includes actions
                    writeDefault(info);
                } else if (obj == null) {
                    out.value((DSElement) null);
                } else if (obj instanceof DSNode) {
                    writeNode(info);
                } else if (obj instanceof DSIValue) {
                    writeValue(info);
                } else {
                    writeObject(info);
                }
            } catch (IndexOutOfBoundsException x) {
                //TODO log a fine - modified during save which is okay.
            }
        }
        out.endList();
    }

    void writeNode(DSInfo arg) {
        out.beginMap();
        if (out instanceof AbstractJsonWriter) {
            ((AbstractJsonWriter) out).writeNewLineIndent();
        }
        out.key("n").value(arg.getName());
        if (!arg.equalsDefaultState()) {
            out.key("i").value(arg.encodeState());
        }
        if (!arg.equalsDefaultType()) {
            DSNode node = (DSNode) arg.getObject();
            if (node != null) {
                out.key("t").value(getToken(node));
            }
        }
        writeChildren((DSNode) arg.getObject());
        out.endMap();
    }

    void writeObject(DSInfo arg) {
        out.beginMap();
        if (out instanceof AbstractJsonWriter) {
            ((AbstractJsonWriter) out).writeNewLineIndent();
        }
        out.key("n").value(arg.getName());
        if (!arg.equalsDefaultState()) {
            out.key("i").value(arg.encodeState());
        }
        if (!arg.equalsDefaultType()) {
            DSIObject obj = arg.getObject();
            if (obj != null) {
                out.key("t").value(getToken(obj));
            }
        }
        out.endMap();
    }

    void writeValue(DSInfo arg) {
        out.beginMap();
        if (out instanceof AbstractJsonWriter) {
            ((AbstractJsonWriter) out).writeNewLineIndent();
        }
        out.key("n").value(arg.getName());
        if (!arg.equalsDefaultState()) {
            out.key("i").value(arg.encodeState());
        }
        if (!arg.equalsDefaultType()) {
            DSIValue v = (DSIValue) arg.getObject();
            if (v != null) {
                out.key("t").value(getToken(v));
                out.key("v").value(v.store());
            } else {
                out.key("v").value((String) null);
            }
        } else if (!arg.equalsDefaultValue()) {
            DSIValue v = (DSIValue) arg.getObject();
            if (v != null) {
                out.key("v").value(v.store());
            } else {
                out.key("v").value((String) null);
            }
        }
        out.endMap();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Initialization
    ///////////////////////////////////////////////////////////////////////////

}
