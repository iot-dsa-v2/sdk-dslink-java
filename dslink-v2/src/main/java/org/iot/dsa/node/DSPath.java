package org.iot.dsa.node;

import java.io.CharArrayWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * Represents a path in the node tree.  Not necessarily a valid one!
 *
 * @author Aaron Hansen
 */
public class DSPath extends DSValue {

    ///////////////////////////////////////////////////////////////////////////
    // Class Fields
    ///////////////////////////////////////////////////////////////////////////

    private static final int caseDiff = ('a' - 'A');
    public static final DSPath NULL = new DSPath("null");
    private static final Charset utf8 = DSString.UTF8;

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    private String path;
    private String[] pathElements;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    private DSPath(String path) {
        this.path = path;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Concatenates the two paths into the leading bucket.  Insures a single forward slash
     * character separates them. The bucket will not be cleared, the path will be appended to it.
     *
     * @param leading Can be null, in which case a new buffer will be created.
     * @return The given bucket, or a new one if that was null, with the complete path appended.
     */
    public static StringBuilder append(StringBuilder leading, String trailing) {
        if (leading == null) {
            leading = new StringBuilder();
        }
        if ((trailing == null) || trailing.isEmpty()) {
            return leading;
        }
        if ((leading.length() > 0) && leading.charAt(leading.length() - 1) == '/') {
            if (trailing.charAt(0) == '/') {
                return leading.append(trailing.substring(1));
            }
        } else {
            if (trailing.charAt(0) != '/') {
                leading.append('/');
            }
        }
        return leading.append(trailing);
    }

    /**
     * Concatenates the two paths into the given bucket.  Insures a single forward slash character
     * separates them. The bucket will not be cleared, the path will be appended to it.
     *
     * @param bucket Can be null, in which case a new buffer will be created.
     * @return The given bucket, or a new one if that was null, with the complete path appended.
     */
    public static StringBuilder concat(String leading, String trailing, StringBuilder bucket) {
        if (bucket == null) {
            bucket = new StringBuilder();
        }
        if ((leading != null) && !leading.isEmpty()) {
            bucket.append(leading);
        }
        if ((trailing == null) || trailing.isEmpty()) {
            return bucket;
        }
        if (!leading.isEmpty() && leading.charAt(leading.length() - 1) == '/') {
            if (trailing.charAt(0) == '/') {
                return bucket.append(trailing.substring(1));
            }
        } else {
            if (trailing.charAt(0) != '/') {
                bucket.append('/');
            }
        }
        return bucket.append(trailing);
    }

    /**
     * Un-escapes a name.
     */
    public static String decodeName(String pathName) {
        boolean modified = false;
        int len = pathName.length();
        StringBuilder sb = new StringBuilder(len > 500 ? len / 2 : len);
        int i = 0;
        char ch;
        byte[] bytes = null;
        while (i < len) {
            ch = pathName.charAt(i);
            switch (ch) {
                case '%':
                    try {
                        if (bytes == null) {
                            bytes = new byte[(len - i) / 3];
                        }
                        int pos = 0;
                        while (((i + 2) < len) && (ch == '%')) {
                            int v = Integer.parseInt(pathName.substring(i + 1, i + 3), 16);
                            if (v < 0) {
                                throw new IllegalArgumentException(
                                        "Illegal hex characters in escape (%) pattern - negative value");
                            }
                            bytes[pos++] = (byte) v;
                            i += 3;
                            if (i < len) {
                                ch = pathName.charAt(i);
                            }
                        }
                        if ((i < len) && (ch == '%')) {
                            throw new IllegalArgumentException(
                                    "Incomplete trailing escape (%) pattern");
                        }
                        sb.append(new String(bytes, 0, pos, utf8));
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException(
                                "Illegal hex characters in escape (%) pattern - " + e.getMessage());
                    }
                    modified = true;
                    break;
                default:
                    sb.append(ch);
                    i++;
                    break;
            }
        }
        return (modified ? sb.toString() : pathName);
    }

    /**
     * Splits the path and decodes each individual name.
     */
    public static String[] decodePath(String path) {
        if (path == null) {
            return new String[0];
        }
        String[] elems = splitPath(path);
        for (int i = 0, len = elems.length; i < len; i++) {
            elems[i] = decodeName(elems[i]);
        }
        return elems;
    }

    /**
     * Encodes a name for being in a path.
     */
    public static String encodeName(String name) {
        StringBuilder builder = new StringBuilder();
        if (encodeName(name, builder, false)) {
            return builder.toString();
        }
        return name;
    }

    /**
     * Encodes a name for being in a path.
     *
     * @param name The raw un-encoded name.
     * @param buf  When to put the encoded name.  Characters will be put into the buf no matter
     *             what.
     * @return True if the name was modified in any way.
     */
    public static boolean encodeName(String name, StringBuilder buf) {
        return encodeName(name, buf, false);
    }

    /**
     * Encodes a DSA v1 name for use outside of a path.
     *
     * @return True if the name was modified in any way.
     */
    public static boolean encodeNameV1(String name, StringBuilder buf) {
        return encodeName(name, buf, true);
    }

    /**
     * Creates a properly encoded path from the given names.
     *
     * @param leadingSlash Whether or not to prepend a slash to the path.
     * @param names        The names to encode in the given order.
     * @return A properly encoded path name.
     */
    public static String encodePath(boolean leadingSlash, String... names) {
        return encodePath(leadingSlash, names, names.length);
    }

    /**
     * Creates a properly encoded path from the given names.
     *
     * @param leadingSlash Whether or not to prepend a slash to the path.
     * @param names        The names to encode in the given order.
     * @param len          The number of elements from the names array to use start at index 0.
     * @return A properly encoded path name.
     */
    public static String encodePath(boolean leadingSlash, String[] names, int len) {
        StringBuilder builder = new StringBuilder();
        if (leadingSlash) {
            builder.append('/');
        }
        for (int i = 0; i < len; i++) {
            if (i > 0) {
                builder.append('/');
            }
            encodeName(names[i], builder);
        }
        return builder.toString();
    }

    /**
     * Ascends the tree and encodes all the node names into a path.
     */
    public static String encodePath(DSNode node) {
        return encodePath(node, null).toString();
    }

    /**
     * Ascends the tree and encodes all the node names into a path.
     *
     * @param buf Optional.
     * @return The given argument, or a new builder if the arg was null.
     */
    public static StringBuilder encodePath(DSNode node, StringBuilder buf) {
        if (buf == null) {
            buf = new StringBuilder();
        }
        ArrayList<DSNode> nodes = new ArrayList<DSNode>();
        while (node != null) {
            if (node.getName() != null) {
                nodes.add(node);
                node = node.getParent();
            } else {
                break;
            }
        }
        for (int i = nodes.size(); --i >= 0; ) {
            buf.append('/');
            encodeName(nodes.get(i).getName(), buf);
        }
        if (buf.length() == 0) {
            buf.append('/');
        }
        return buf;
    }

    public String getLastPathElement() {
        String[] elements = getPathElements();
        if ((elements == null) || (elements.length == 0)) {
            return null;
        }
        return elements[elements.length - 1];
    }

    /**
     * The raw fully encoded path.
     */
    public String getPath() {
        return path;
    }

    /**
     * The individual, decoded path elements.
     */
    public String[] getPathElements() {
        if (pathElements == null) {
            pathElements = decodePath(path);
        }
        return pathElements;
    }

    @Override
    public DSValueType getValueType() {
        return DSValueType.STRING;
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }

    @Override
    public boolean isNull() {
        return this == NULL;
    }

    /**
     * Splits the path, but does not decode any names.  The difference between this and
     * String.split() is that String.split() will return an empty string at index 0 if the path
     * starts with /.
     *
     * @return Never null, but could be an empty array.
     */
    public static String[] splitPath(String path) {
        if ((path == null) || path.isEmpty() || path.equals("/")) {
            return new String[0];
        }
        int startIdx = 0;
        int endIdx = path.length();
        boolean mod = false;
        if (path.charAt(0) == '/') {
            startIdx++;
            mod = true;
        }
        if (path.charAt(endIdx - 1) == '/') {
            endIdx--;
            mod = true;
        }
        if (mod) {
            if (startIdx >= endIdx) {
                return new String[0];
            }
            path = path.substring(startIdx, endIdx);
        }
        return path.split("/");
    }

    @Override
    public DSString toElement() {
        if (this == NULL) {
            return DSString.NULL;
        }
        return DSString.valueOf(path);
    }

    @Override
    public String toString() {
        return path;
    }

    @Override
    public DSPath valueOf(DSElement element) {
        if (element.isNull()) {
            return NULL;
        }
        return new DSPath(element.toString());
    }

    public static DSPath valueOf(String path) {
        if (path == null) {
            return NULL;
        }
        return new DSPath(path);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Private Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Encodes a name.
     *
     * @param name The raw un-encoded name.
     * @param buf  When to put the encoded name.  Characters will be put into the buf no matter
     *             what.
     * @param v1   True if for v1.
     * @return True if the name was modified in any way.
     */
    private static boolean encodeName(String name, StringBuilder buf, boolean v1) {
        if (name == null) {
            return false;
        }
        boolean modified = false;
        int pathLength = name.length();
        CharArrayWriter charArrayWriter = null;
        char c;
        boolean encode = false;
        for (int i = 0; i < pathLength; ) {
            c = name.charAt(i);
            encode = v1 ? shouldEncodeV1(c) : shouldEncode(c);
            if (!encode) {
                buf.append(c);
                i++;
            } else {
                if (charArrayWriter == null) {
                    charArrayWriter = new CharArrayWriter();
                }
                do {
                    charArrayWriter.write(c);
                    if (c >= 0xD800 && c <= 0xDBFF) {
                        if ((i + 1) < name.length()) {
                            int d = (int) name.charAt(i + 1);
                            if (d >= 0xDC00 && d <= 0xDFFF) {
                                charArrayWriter.write(d);
                                i++;
                            }
                        }
                    }
                    i++;
                } while (i < pathLength && shouldEncode((c = name.charAt(i))));
                charArrayWriter.flush();
                String str = new String(charArrayWriter.toCharArray());
                byte[] bytes = str.getBytes(utf8);
                for (int j = 0, blen = bytes.length; j < blen; j++) {
                    buf.append('%');
                    char ch = Character.forDigit((bytes[j] >> 4) & 0xF, 16);
                    if (Character.isLetter(ch)) {
                        ch -= caseDiff;
                    }
                    buf.append(ch);
                    ch = Character.forDigit(bytes[j] & 0xF, 16);
                    if (Character.isLetter(ch)) {
                        ch -= caseDiff;
                    }
                    buf.append(ch);
                }
                charArrayWriter.reset();
                modified = true;
            }
        }
        return modified;
    }

    /**
     * Returns true for characters that should be encoded.
     */
    private static boolean shouldEncode(int ch) {
        switch (ch) {
            case '.':
            case '/':
            case '\\':
            case '\'':
            case '"':
            case '?':
            case '*':
            case '|':
            case '<':
            case '>':
            case '=':
            case ':':
            case ';':
            case '%':
                return true;
            default:
                return ch < 0x20;
        }
    }

    /**
     * Returns true for characters that should be encoded.
     */
    private static boolean shouldEncodeV1(int ch) {
        switch (ch) {
            case '/':
            case '%':
                return true;
            default:
                return ch < 0x20;
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Initialization
    ///////////////////////////////////////////////////////////////////////////

    static {
        DSRegistry.registerDecoder(DSPath.class, NULL);
    }

}
