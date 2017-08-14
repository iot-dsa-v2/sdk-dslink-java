package org.iot.dsa.dslink;

import org.iot.dsa.io.DSByteBuffer;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Aaron Hansen
 */
public class DSByteBufferTest {

    // Constants
    // ---------

    // Fields
    // ------

    // Constructors
    // ------------

    // Methods
    // -------

    @Test
    public void theTest() throws Exception {
        DSByteBuffer buffer = new DSByteBuffer(5);
        Assert.assertFalse(buffer.isOpen());
        buffer.open();
        Assert.assertTrue(buffer.available() == 0);
        byte[] tmp = new byte[]{1, 2, 3, 4, 5};
        buffer.put(tmp, 0, tmp.length);
        Assert.assertTrue(buffer.available() == tmp.length);
        int len = buffer.read(tmp, 0, tmp.length);
        Assert.assertTrue(len == tmp.length);
        Assert.assertArrayEquals(tmp, new byte[]{1, 2, 3, 4, 5});
        buffer.put(tmp, 0, tmp.length);
        Assert.assertTrue(buffer.read() == 1);
        Assert.assertTrue(buffer.read() == 2);
        Assert.assertTrue(buffer.read() == 3);
        Assert.assertTrue(buffer.read() == 4);
        Assert.assertTrue(buffer.read() == 5);
        Assert.assertTrue(buffer.available() == 0);
        buffer.put(tmp, 0, tmp.length);
        buffer.put(tmp, 0, tmp.length);
        Assert.assertTrue(buffer.available() == 10);
        Assert.assertTrue(buffer.read() == 1);
        Assert.assertTrue(buffer.read() == 2);
        Assert.assertTrue(buffer.read() == 3);
        Assert.assertTrue(buffer.read() == 4);
        Assert.assertTrue(buffer.read() == 5);

        Assert.assertTrue(buffer.read() == 1);
        Assert.assertTrue(buffer.read() == 2);
        Assert.assertTrue(buffer.read() == 3);
        Assert.assertTrue(buffer.read() == 4);
        Assert.assertTrue(buffer.read() == 5);
        Assert.assertTrue(buffer.available() == 0);
        buffer.put(tmp, 0, tmp.length);
        buffer.put(tmp, 0, tmp.length);
        Assert.assertTrue(buffer.read() == 1);
        Assert.assertTrue(buffer.read() == 2);
        buffer.put(tmp, 0, tmp.length);
        Assert.assertTrue(buffer.read() == 3);
    }

// Inner Classes
// -------------
//

}
