package org.iot.dsa.node;

import java.io.CharArrayWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * @author Aaron Hansen
 */
public class DSPath {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    private static final int caseDiff = ('a' - 'A');
    private static final Charset utf8 = Charset.forName("UTF-8");

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private String[] pathElements;
    private String path;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public DSPath(String path) {
        this.path = path;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods in alphabetical order
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Un-escapes a name.
     */
    public static String decodePathName(String pathName) {
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
        String[] elems = splitPath(path);
        for (int i = 0, len = elems.length; i < len; i++) {
            elems[i] = decodePathName(elems[i]);
        }
        return elems;
    }

    /**
     * Ascends the tree and creates pretty printing path.
     */
    public static String encodeDisplayPath(DSNode node) {
        ArrayList<DSNode> nodes = new ArrayList<DSNode>();
        while (node != null) {
            if (node.getName() != null) {
                nodes.add(node);
                break;
            }
            node = node.getParent();
        }
        StringBuilder builder = new StringBuilder();
        for (int i = nodes.size(); --i >= 0; ) {
            builder.append('/');
            builder.append(node.getName());
        }
        return builder.toString();
    }

    /**
     * Ascends the tree and encodes all the node names into a path.
     */
    public static String encodePath(DSNode node) {
        ArrayList<DSNode> nodes = new ArrayList<DSNode>();
        while (node != null) {
            if (node.getName() != null) {
                nodes.add(node);
                node = node.getParent();
            } else {
                break;
            }
        }
        StringBuilder builder = new StringBuilder();
        for (int i = nodes.size(); --i >= 0; ) {
            builder.append('/');
            encodeName(node.getName(), builder);
        }
        return builder.toString();
    }

    /**
     * Encodes a name for being in a path.
     */
    public static String encodeName(String name) {
        StringBuilder builder = new StringBuilder();
        if (encodeName(name, builder)) {
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
        if (name == null) {
            return false;
        }
        boolean modified = false;
        int pathLength = name.length();
        CharArrayWriter charArrayWriter = new CharArrayWriter();
        char c;
        for (int i = 0; i < pathLength; ) {
            c = name.charAt(i);
            if (!shouldEncode(c)) {
                buf.append((char) c);
                i++;
            } else {
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

    /**
     * /**
     * Returns true for characters that should be encoded.
     */
    public static boolean shouldEncode(int ch) {
        switch (ch) {
            case '.':
            case '/':
            case '\\':
            case '?':
            case '*':
            case ':':
            case '"':
            case '<':
            case '>':
            case '%':
                return true;
            default:
                return false;
        }
    }

    /**
     * Splits the path, but does not decodeKeys any names.
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

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Initialization
    ///////////////////////////////////////////////////////////////////////////

}
