package org.iot.dsa.io.json;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Json implementation of DSWriter intended for OutputStreams and Writers.  While
 * JsonAppender can also handle OutputStreams and Writer, this is more performant.
 * <p>The same instance can be reused with the setOutput methods.</p>
 * <p>This class is not thread safe.</p>
 *
 * @author Aaron Hansen
 * @see org.iot.dsa.io.DSWriter
 */
public class JsonWriter extends AbstractJsonWriter {

    // Constants
    // ---------

    // Fields
    // ------

    private char[] buf = new char[BUF_SIZE];
    private int buflen = 0;
    private Writer out;
    private boolean zip = false;
    private ZipOutputStream zout;

    // Constructors
    // ------------

    /**
     * Be sure to call one of the setOutput methods.
     */
    public JsonWriter() {
    }

    /**
     * Creates an underlying FileWriter.
     */
    public JsonWriter(File arg) {
        setOutput(arg);
    }

    /**
     * Will create a zip file using the zipFileName as file name inside the zip.
     */
    public JsonWriter(File file, String zipFileName) {
        setOutput(file, zipFileName);
    }

    /**
     * Creates an underlying OutputStreamWriter.
     */
    public JsonWriter(OutputStream arg) {
        setOutput(arg);
    }

    /**
     * Will write a zip file to the given stream.
     */
    public JsonWriter(OutputStream out, String zipFileName) {
        setOutput(out, zipFileName);
    }

    @Override
    public void close() {
        try {
            flush();
            if (getDepth() > 0) {
                throw new IllegalStateException("Nesting error.");
            }
            if (zout != null) {
                try {
                    zout.closeEntry();
                } catch (Exception x) {
                }
                zout.close();
                zout = null;
            } else {
                out.close();
            }
        } catch (IOException x) {
            throw new RuntimeException(x);
        }
    }

    @Override
    public JsonWriter flush() {
        try {
            if (buflen > 0) {
                out.write(buf, 0, buflen);
                buflen = 0;
            }
            out.flush();
        } catch (IOException x) {
            throw new RuntimeException(x);
        }
        return this;
    }

    @Override
    public JsonWriter reset() {
        buflen = 0;
        return (JsonWriter) super.reset();
    }

    /**
     * Sets the sink, resets the state and returns this.
     */
    public JsonWriter setOutput(File arg) {
        try {
            if (arg == null) {
                throw new NullPointerException();
            }
            this.out = new FileWriter(arg);
        } catch (IOException x) {
            throw new RuntimeException(x);
        }
        return reset();
    }

    /**
     * Will create a zip file using the zipFileName as file name inside the zip. Resets the state
     * and returns this.
     */
    public JsonWriter setOutput(File file, String zipFileName) {
        try {
            if (file == null) {
                throw new NullPointerException();
            }
            zout = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
            zout.putNextEntry(new ZipEntry(zipFileName));
            this.out = new OutputStreamWriter(zout);
            this.zip = true;
        } catch (IOException x) {
            throw new RuntimeException(x);
        }
        return reset();
    }

    /**
     * Sets the sink, resets the state and returns this.
     */
    public JsonWriter setOutput(OutputStream arg) {
        if (arg == null) {
            throw new NullPointerException();
        }
        this.out = new OutputStreamWriter(arg);
        return reset();
    }

    /**
     * Will write a zip file to the given stream. Resets the state and returns this.
     */
    public JsonWriter setOutput(OutputStream out, String zipFileName) {
        try {
            if (out == null) {
                throw new NullPointerException();
            }
            if (zipFileName == null) {
                throw new NullPointerException();
            }
            ZipOutputStream zout = new ZipOutputStream(out);
            zout.putNextEntry(new ZipEntry(zipFileName));
            this.out = new OutputStreamWriter(zout);
            this.zip = true;
        } catch (IOException x) {
            throw new RuntimeException(x);
        }
        return reset();
    }

    /**
     * Sets the sink, resets the state and returns this.
     */
    public JsonWriter setOutput(Writer out) {
        this.out = out;
        return reset();
    }

    @Override
    public Appendable append(CharSequence csq) throws IOException {
        for (int i = 0, len = csq.length(); i < len; i++) {
            append(csq.charAt(i));
        }
        return this;
    }

    @Override
    public Appendable append(char ch) throws IOException {
        if (buflen + 1 >= BUF_SIZE) {
            flush();
        }
        buf[buflen++] = ch;
        return this;
    }

    /**
     * Append the chars and return this. Can be used for custom formatting.
     */
    @Override
    public Appendable append(CharSequence csq, int start, int end) throws IOException {
        for (int i = start; i < end; i++) {
            append(csq.charAt(i));
        }
        return this;
    }

    /**
     * Append the chars and return this. Can be used for custom formatting.
     */
    @Override
    public AbstractJsonWriter append(char[] ch, int off, int len) {
        if (buflen + len >= BUF_SIZE) {
            flush();
        }
        System.arraycopy(ch, off, buf, buflen, len);
        buflen += len;
        return this;
    }

}
