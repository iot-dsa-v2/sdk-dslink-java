package org.iot.dsa.dslink;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import org.iot.dsa.node.DSIObject;

/**
 * Binds an DSLinkConnection to a binary or text transport implementation.  Examples of transports would
 * be sockets, websockets and http.
 * <p>
 * Subclasses should call and override all protected methods.
 *
 * @author Aaron Hansen
 */
public interface DSITransport extends DSIObject {

    ///////////////////////////////////////////////////////////////////////////
    // Class Fields
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Called at the start of a new inbound message.
     */
    void beginRecvMessage();

    /**
     * Called at the start of a new outbound message.
     */
    void beginSendMessage();

    /**
     * Close the actual connection and clean up resources.  Calling when already closed will have no
     * effect.
     */
    void close();

    /**
     * Called at the end of a message, this trace logs the entire message.
     */
    void endRecvMessage();

    /**
     * Called at the end of a message, this ensures that message based transports know the message
     * is complete and this also trace logs the entire message.
     */
    void endSendMessage();

    /**
     * For reading binary data, only use if isText() is false.
     */
    InputStream getBinaryInput();

    /**
     * For writing binary data, only use if isText() is false.
     */
    OutputStream getBinaryOutput();

    /**
     * If not explicitly set, searches for the ancestor.
     */
    DSLinkConnection getConnection();

    /**
     * For reading text, only use if isText() is true.
     */
    Reader getTextInput();

    /**
     * For writing text, only use if isText() is true.
     */
    Writer getTextOutput();

    /**
     * Whether or not the transport is open for reading and writing.
     */
    boolean isOpen();

    /**
     * True if the transport is text based, and the text IO methods should be used.
     */
    boolean isText();

    void open();

    /**
     * Blocking read operation, returns the number of bytes read.
     public int read(byte[] buf, int off, int len);
     */

    /**
     * Blocking read operation, returns the number of chars read.
     public int read(char[] buf, int off, int len);
     */

    /**
     * Write binary data, only use if isText() is false.
     *
     * @param buf    The buffer containing the bytes to write.
     * @param off    The index in the buffer of the first byte to write.
     * @param len    The number of bytes to write.
     * @param isLast Indicator of end of frame (message) for frame oriented transports such as
     *               websockets.
    public void write(byte[] buf, int off, int len, boolean isLast);
     */

    /**
     * Write text, only use if isText() returns true.
     *
     * @param msgPart The characters to send
     * @param isLast  Whether or not this completes the message.
    public void write(String msgPart, boolean isLast);
     */

    /**
     * Write text, only use if isText() returns true.
     *
     * @param buf    The characters to send
     * @param off    Starting offset in the buf.
     * @param len    Number of chars to write.
     * @param isLast Whether or not this completes the message.
    public void write(char[] buf, int off, int len, boolean isLast);
     */

    /**
     * The size of the current outbound message (bytes for binary, chars for text).
     */
    int writeMessageSize();

}
