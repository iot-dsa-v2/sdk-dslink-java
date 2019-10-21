package com.acuity.iot.dsa.dslink.transport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;

/**
 * Abstract websocket transport.
 *
 * @author Aaron Hansen
 */
public abstract class DSTransportStream extends DSTransport {

    ///////////////////////////////////////////////////////////////////////////
    // Class Fields
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    private InputStream in;
    private OutputStream out;
    private Reader reader;
    private Writer writer;

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void close() {
        super.close();
        try {
            if (in != null) {
                in.close();
                in = null;
            }
        } catch (IOException x) {
            debug("", x);
        }
        try {
            if (out != null) {
                out.close();
                out = null;
            }
        } catch (IOException x) {
            debug("", x);
        }
        try {
            if (reader != null) {
                reader.close();
                reader = null;
            }
        } catch (IOException x) {
            debug("", x);
        }
        try {
            if (writer != null) {
                writer.close();
                writer = null;
            }
        } catch (IOException x) {
            debug("", x);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected Methods
    ///////////////////////////////////////////////////////////////////////////


    @Override
    protected int available() {
        int ret;
        try {
            ret = in.available();
        } catch (IOException x) {
            throw new UncheckedIOException(x);
        }
        return ret;
    }

    @Override
    protected int doRead(byte[] buf, int off, int len) {
        int ret = 0;
        try {
            ret = in.read(buf, off, len);
        } catch (IOException x) {
            close(x);
        }
        return ret;
    }

    @Override
    protected int doRead(char[] buf, int off, int len) {
        int ret = 0;
        try {
            ret = reader.read(buf, off, len);
        } catch (IOException x) {
            close(x);
        }
        return ret;
    }

    @Override
    protected void doWrite(byte[] buf, int off, int len, boolean isLast) {
        try {
            out.write(buf, off, len);
        } catch (IOException x) {
            throw new UncheckedIOException(x);
        }
    }

    @Override
    protected void doWrite(String msgPart, boolean isLast) {
        try {
            writer.write(msgPart);
        } catch (IOException x) {
            throw new UncheckedIOException(x);
        }
    }

    /**
     * Call to open a text transport
     */
    protected void init(Reader in, Writer out) {
        this.reader = in;
        this.writer = out;
        setText(true);
        setOpen();
    }

    /**
     * Call to open a binary transport
     */
    protected void open(InputStream in, OutputStream out) {
        this.in = in;
        this.out = out;
        setText(false);
        setOpen();
    }

}
