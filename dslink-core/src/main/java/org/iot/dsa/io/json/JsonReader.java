package org.iot.dsa.io.json;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import org.iot.dsa.io.AbstractReader;
import org.iot.dsa.io.DSIReader;
import org.iot.dsa.node.DSBytes;

/**
 * Json implementation of DSReader.  The same instance can be re-used with the setInput methods.
 * This class is not thread safe.
 *
 * @author Aaron Hansen
 * @see DSIReader
 */
public class JsonReader extends AbstractReader implements DSIReader, JsonConstants {

    // Constants
    // ---------

    private static final int BUFLEN = 8192;

    // Fields
    // ---------

    private char[] buf = new char[BUFLEN];
    private int buflen = 0;
    private Input in;

    // Constructors
    // ------------

    public JsonReader() {
    }

    public JsonReader(CharSequence in) {
        setInput(in);
    }

    public JsonReader(File file) {
        setInput(file);
    }

    public JsonReader(InputStream in, String charset) {
        setInput(in, charset);
    }

    public JsonReader(java.io.Reader in) {
        setInput(in);
    }

    // Public Methods
    // --------------

    @Override
    public void close() {
        try {
            in.close();
        } catch (Exception x) {
            throw new RuntimeException(x);
        }
    }

    @Override
    public Token next() {
        try {
            int ch;
            boolean hasValue = false;
            while (true) {
                ch = readNextClue();
                switch (ch) {
                    case '[':
                        return setBeginList();
                    case '{':
                        return setBeginMap();
                    case ':':
                        if (last() != Token.STRING) {
                            throw new IllegalStateException("Invalid key");
                        }
                        return last();
                    case ',':
                        if ((last() == Token.END_LIST) || (last() == Token.END_MAP)) {
                            break;
                        }
                        return last();
                    case ']':
                        if (!hasValue) {
                            return setEndList();
                        }
                        in.unread();
                        return last();
                    case '}':
                        if (!hasValue) {
                            return setEndMap();
                        }
                        in.unread();
                        return last();
                    case -1:
                        if (!hasValue) {
                            return setEndInput();
                        }
                        in.unread();
                        return last();
                    // values
                    case '"':
                        readString();
                        String str = valString;
                        hasValue = true;
                        if ((str.length() > 0) && (str.charAt(0) == '\u001B')) {
                            if (DSBytes.isBytes(str)) {
                                setNextValue(DSBytes.decode(str));
                                break;
                            }
                            if (str.equals(DBL_NEG_INF)) {
                                setNextValue(Double.NEGATIVE_INFINITY);
                                break;
                            }
                            if (str.equals(DBL_POS_INF)) {
                                setNextValue(Double.POSITIVE_INFINITY);
                                break;
                            }
                            if (str.equals(DBL_NAN)) {
                                setNextValue(Double.NaN);
                                break;
                            }
                        }
                        setNextValue(valString);
                        break;
                    case 't':
                        validate(in.read(), 'r');
                        validate(in.read(), 'u');
                        validate(in.read(), 'e');
                        setNextValue(true);
                        hasValue = true;
                        break;
                    case 'f':
                        validate(in.read(), 'a');
                        validate(in.read(), 'l');
                        validate(in.read(), 's');
                        validate(in.read(), 'e');
                        setNextValue(false);
                        hasValue = true;
                        break;
                    case 'n':
                        validate(in.read(), 'u');
                        validate(in.read(), 'l');
                        validate(in.read(), 'l');
                        setNextValueNull();
                        hasValue = true;
                        break;
                    case '-':
                    case '.': // can number start with '.'?
                    case '0':
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                        readNumber((char) ch);
                        hasValue = true;
                }
            }
        } catch (IOException x) {
            throw new RuntimeException(x);
        }
    }

    @Override
    public JsonReader reset() {
        super.reset();
        return this;
    }

    /**
     * Sets the input source, resets to ROOT, and returns this.
     */
    public JsonReader setInput(CharSequence in) {
        this.in = new CharSequenceInput(in);
        return reset();
    }

    /**
     * Sets the input source, resets to ROOT, and returns this.
     */
    public JsonReader setInput(File file) {
        try {
            if (in instanceof JsonInput) {
                ((JsonInput) in).setInput(new BufferedInputStream(new FileInputStream(file)));
            } else {
                in = new JsonInput(new BufferedInputStream(new FileInputStream(file)));
            }
        } catch (Exception x) {
            throw new IllegalStateException(x);
        }
        return reset();
    }

    /**
     * Sets the input source, resets to ROOT, and returns this.
     */
    public JsonReader setInput(InputStream inputStream, String charset) {
        try {
            if (this.in instanceof JsonInput) {
                ((JsonInput) this.in).setInput(inputStream, charset);
            } else {
                this.in = new JsonInput(inputStream, charset);
            }
            return reset();
        } catch (IOException x) {
            throw new IllegalStateException("IOException: " + x.getMessage(), x);
        }
    }

    /**
     * Sets the input source, resets to ROOT, and returns this.
     */
    public JsonReader setInput(Reader reader) {
        if (this.in instanceof JsonInput) {
            ((JsonInput) this.in).setInput(reader);
        } else {
            this.in = new JsonInput(reader);
        }
        return reset();
    }

    // Private Methods
    // ---------------

    private void bufAppend(char ch) {
        if (buflen == buf.length) {
            char[] tmp = new char[buflen * 2];
            System.arraycopy(buf, 0, tmp, 0, buflen);
            buf = tmp;
        }
        buf[buflen++] = ch;
    }

    private String bufToString() {
        String ret = new String(buf, 0, buflen);
        buflen = 0;
        return ret;
    }

    /**
     * Scans for the nextRun relevant character, skipping over whitespace etc.
     */
    private int readNextClue() throws IOException {
        int ch = in.read();
        while (ch >= 0) {
            switch (ch) {
                case '{':
                case '}':
                case '[':
                case ']':
                case ':':
                case ',':
                case '"':
                case 't':
                case 'f':
                case 'n':
                case '-':
                case '.':
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    return ch;
            }
            ch = in.read();
        }
        return ch;
    }

    private Token readNumber(char clue) throws IOException {
        char ch = clue;
        boolean hasDecimal = false;
        boolean hasMore = true;
        while (hasMore) {
            switch (ch) {
                case '.':
                case 'e':
                case 'E':
                    hasDecimal = true;
                case '-':
                case '+':
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    bufAppend(ch);
                    ch = (char) in.read();
                    break;
                default:
                    in.unread();
                    hasMore = false;
            }
        }
        if (hasDecimal) {
            return setNextValue(Double.parseDouble(bufToString()));
        }
        long l = Long.parseLong(bufToString());
        return setNextValue(l);
    }

    private void readString() throws IOException {
        char ch = (char) in.read();
        while (ch != '"') {
            if (ch == '\\') {
                ch = (char) in.read();
                switch (ch) {
                    case 'u':
                    case 'U':
                        ch = readUnicode();
                        break;
                    case 'b':
                        ch = '\b';
                        break;
                    case 'f':
                        ch = '\f';
                        break;
                    case 'n':
                        ch = '\n';
                        break;
                    case 'r':
                        ch = '\r';
                        break;
                    case 't':
                        ch = '\t';
                        break;
                    case '"':
                    case '\\':
                    case '/':
                        break;
                    default:
                        throw new IOException("Unexpected escape: \\" + ch);
                }
            }
            bufAppend(ch);
            ch = (char) in.read();
        }
        valString = bufToString();
    }

    private char readUnicode() throws IOException {
        int ret = 0;
        int ch;
        for (int i = 0; i < 4; ++i) {
            switch (ch = in.read()) {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    ret = (ret << 4) + ch - '0';
                    break;
                case 'a':
                case 'b':
                case 'c':
                case 'd':
                case 'e':
                case 'f':
                    ret = (ret << 4) + (ch - 'a') + 10;
                    break;
                case 'A':
                case 'B':
                case 'C':
                case 'D':
                case 'E':
                case 'F':
                    ret = (ret << 4) + (ch - 'A') + 10;
                    break;
                default:
                    throw new IllegalStateException(
                            "Illegal character in unicode escape: " + (char) ch);
            }
        }
        return (char) ret;
    }

    private static void validate(int ch1, int ch2) {
        if (ch1 != ch2) {
            throw new IllegalStateException("Expecting " + (char) ch2 + ", not " + (char) ch1);
        }
    }

    // Inner Classes
    // -------------

    /**
     * Needed for the ability to unread (pushback) a char.
     */
    static interface Input extends Closeable {

        public int read() throws IOException;

        public void unread();
    }

}
