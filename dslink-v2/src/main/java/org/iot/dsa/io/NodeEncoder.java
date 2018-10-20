package org.iot.dsa.io;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import org.iot.dsa.io.json.AbstractJsonWriter;
import org.iot.dsa.io.json.JsonWriter;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSIObject;
import org.iot.dsa.node.DSIStorable;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSNode;

/**
 * Encodes a node tree using a compact JSON schema.  Defaults are omitted and class names are
 * tokenized to minimize size. Use NodeDecoder for deserialization.
 * <p>
 * This is for saving a configuration database, not for DSA interop.
 *
 * @author Aaron Hansen
 * @see NodeDecoder
 */
public class NodeEncoder {

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSMap cacheMap = new DSMap();
    private HashMap<Class, String> classMap = new HashMap<Class, String>();
    private int nextToken = 1;
    private DSIWriter out;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    NodeEncoder(DSIWriter out) {
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
     */
    public static void encode(DSIWriter out, DSNode node) {
        NodeEncoder encoder = new NodeEncoder(out);
        encoder.write(node);
        out.flush();
    }

    /**
     * Encodes the node to a byte array.
     *
     * @param node What to encode.
     * @return Json serialized node.
     */
    public static byte[] encode(DSNode node) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        JsonWriter writer = new JsonWriter(bos);
        NodeEncoder.encode(writer, node);
        writer.close();
        return bos.toByteArray();
    }

    /**
     * Prints the JSON encoded node to System.out.
     *
     * @param node What to encode.
     */
    public static void print(DSNode node) {
        JsonWriter writer = new JsonWriter(System.out );
        NodeEncoder.encode(writer, node);
        System.out.flush();
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
        info = arg.getFirstInfo();
        while (info != null) {
            try {
                obj = info.getObject();
                if (info.isTransient()) {
                    ;//skip it
                } else if (info.equalsDefault()) {  //includes actions
                    writeDefault(info);
                } else if (obj == null) {
                    out.value((DSElement) null);
                } else if (info.isNode()) {
                    writeNode(info);
                } else if (info.isValue()) {
                    writeValue(info);
                }
            } catch (IndexOutOfBoundsException x) {
                arg.trace(arg.getPath(), x);
            }
            info = info.next();
        }
        out.endList();
    }

    void writeDefault(DSInfo arg) {
        out.beginMap();
        if (out instanceof AbstractJsonWriter) {
            ((AbstractJsonWriter) out).writeNewLineIndent();
        }
        out.key("n").value(arg.getName());
        out.endMap();
    }

    void writeInfo(DSInfo arg) {
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
        if (!arg.equalsDefaultMetadata()) {
            arg.getMetadata(cacheMap);
            if (!cacheMap.isEmpty()) {
                out.key("m").value(cacheMap);
                cacheMap.clear();
            }
        }
    }

    void writeNode(DSInfo arg) {
        out.beginMap();
        writeInfo(arg);
        writeChildren((DSNode) arg.getObject());
        out.endMap();
    }

    void writeValue(DSInfo arg) {
        out.beginMap();
        writeInfo(arg);
        DSIValue v = arg.getValue();
        if (!arg.equalsDefaultValue()) {
            if (v instanceof DSIStorable) {
                out.key("v").value(((DSIStorable) v).store());
            } else {
                out.key("v").value(v.toElement());
            }
        }
        out.endMap();
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

}
