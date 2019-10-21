package org.iot.dsa.io.json;

import java.io.Closeable;
import java.io.IOException;

/**
 * Wraps a CharSequence for DSReader.
 *
 * @author Aaron Hansen
 */
class CharSequenceInput implements JsonReader.Input {

    private CharSequence in;
    private int len;
    private int next = 0;

    public CharSequenceInput(CharSequence in) {
        this.in = in;
        this.len = in.length();
    }

    public void close() throws IOException {
        if (in instanceof Closeable) {
            ((Closeable) in).close();
        }
    }

    public int read() {
        if (next >= len) {
            return -1;
        }
        return in.charAt(next++);
    }

    public void unread() {
        next--;
    }

}
