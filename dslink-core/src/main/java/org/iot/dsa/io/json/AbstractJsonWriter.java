package org.iot.dsa.io.json;

import java.io.IOException;
import org.iot.dsa.io.AbstractWriter;
import org.iot.dsa.io.DSIWriter;
import org.iot.dsa.node.DSBytes;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.util.DSException;

/**
 * @author Aaron Hansen
 */
public abstract class AbstractJsonWriter extends AbstractWriter
        implements Appendable, DSIWriter, JsonConstants {

    // Constants
    // ---------

    static final int BUF_SIZE = 8192;
    private static final char[] C_B = new char[]{'\\', 'b'};
    private static final char[] C_F = new char[]{'\\', 'f'};
    private static final char[] C_FALSE = new char[]{'f', 'a', 'l', 's', 'e'};
    private static final char[] C_INDENT = new char[]{' ', ' '};
    private static final char[] C_N = new char[]{'\\', 'n'};
    private static final char[] C_NULL = new char[]{'n', 'u', 'l', 'l'};
    private static final char[] C_R = new char[]{'\\', 'r'};
    private static final char[] C_T = new char[]{'\\', 't'};
    private static final char[] C_TRUE = new char[]{'t', 'r', 'u', 'e'};
    private static final char[] C_U = new char[]{'\\', 'u'};
    private static final char[] HEX =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    // Public Methods
    // --------------

    /**
     * Append the characters and return this.
     */
    public abstract AbstractJsonWriter append(char[] ch, int off, int len);

    public AbstractJsonWriter setPrettyPrint(boolean arg) {
        prettyPrint = arg;
        return this;
    }

    @Override
    public AbstractWriter value(DSElement arg) {
        if ((arg != null) && arg.isBoolean()) {
            return value(arg.toString());
        }
        return super.value(arg);
    }

    // Protected Methods
    // -----------------

    @Override
    protected void writeSeparator() throws IOException {
        append(',');
    }

    /**
     * Two spaces per level.
     */
    public void writeNewLineIndent() {
        try {
            append('\n');
            for (int i = getDepth(); --i >= 0; ) {
                append(C_INDENT, 0, 2);
            }
        } catch (IOException x) {
            DSException.throwRuntime(x);
        }
    }

    @Override
    protected void write(boolean arg) throws IOException {
        if (arg) {
            append(C_TRUE, 0, 4);
        } else {
            append(C_FALSE, 0, 5);
        }
    }

    @Override
    protected void write(byte[] arg) throws IOException {
        writeValue(DSBytes.encode(arg));
    }

    @Override
    protected void write(double arg) throws IOException {
        if ((arg % 1) == 0) {
            write((long) arg);
        } else if (Double.isInfinite(arg)) {
            if (arg < 0) {
                append(DBL_NEG_INF);
            } else {
                append(DBL_POS_INF);
            }
        } else if (Double.isNaN(arg)) {
            append(DBL_NAN);
        } else {
            append(String.valueOf(arg));
        }
    }

    @Override
    protected void write(long arg) throws IOException {
        append(String.valueOf(arg));
    }

    @Override
    protected void writeKey(CharSequence arg) throws IOException {
        writeString(arg);
    }

    @Override
    protected void writeKeyValueSeparator() throws IOException {
        if (prettyPrint) {
            append(" : ");
        } else {
            append(':');
        }
    }

    @Override
    protected void writeListEnd() throws IOException {
        append(']');
    }

    @Override
    protected void writeListStart() throws IOException {
        append('[');
    }

    @Override
    protected void writeMapEnd() throws IOException {
        append('}');
    }

    @Override
    protected void writeMapStart() throws IOException {
        append('{');
    }

    @Override
    protected void writeNull() throws IOException {
        append(C_NULL, 0, 4);
    }

    @Override
    protected void writeValue(CharSequence arg) throws IOException {
        writeString(arg);
    }

    // Private Methods
    // ---------------

    /**
     * Encodes a string.
     */
    private void writeString(Object arg) throws IOException {
        String s = String.valueOf(arg);
        append('"');
        char ch;
        for (int i = 0, len = s.length(); i < len; i++) {
            ch = s.charAt(i);
            switch (ch) {
                case '"':
                case '\\':
                    append('\\');
                    append(ch);
                    break;
                case '\b':
                    append(C_B, 0, 2);
                    break;
                case '\f':
                    append(C_F, 0, 2);
                    break;
                case '\n':
                    append(C_N, 0, 2);
                    break;
                case '\r':
                    append(C_R, 0, 2);
                    break;
                case '\t':
                    append(C_T, 0, 2);
                    break;
                default:
                    if (Character.isISOControl(ch)) {
                        writeUnicode(ch);
                    } else {
                        append(ch);
                    }
            }
        }
        append('"');
    }

    /**
     * Encode a unicode char.
     */
    private void writeUnicode(char ch) throws IOException {
        append(C_U, 0, 2);
        append(HEX[(ch >>> 12) & 0xf]);
        append(HEX[(ch >>> 8) & 0xf]);
        append(HEX[(ch >>> 4) & 0xf]);
        append(HEX[(ch) & 0xf]);
    }

}
