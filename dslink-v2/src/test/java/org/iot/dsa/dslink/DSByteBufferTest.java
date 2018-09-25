package org.iot.dsa.dslink;

import com.acuity.iot.dsa.dslink.io.DSByteBuffer;
import java.nio.ByteBuffer;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Aaron Hansen
 */
public class DSByteBufferTest {

    @Test
    public void comparisonTest() {
        ByteBuffer javaBuf = ByteBuffer.allocate(1024);
        javaBuf.put((byte) 1);
        DSByteBuffer dsBuf = new DSByteBuffer();
        dsBuf.put((byte) 1);
        byte[] dsry = dsBuf.toByteArray();
        byte[] jary = toByteArray(javaBuf);
        Assert.assertEquals(dsry.length, jary.length);
        Assert.assertEquals(dsry, jary);
        //double
        javaBuf.putDouble(12345.6);
        dsBuf.putDouble(12345.6);
        dsry = dsBuf.toByteArray();
        jary = toByteArray(javaBuf);
        Assert.assertEquals(dsry.length, jary.length);
        Assert.assertEquals(dsry, jary);
        //int
        javaBuf.putInt(12345);
        dsBuf.putInt(12345);
        dsry = dsBuf.toByteArray();
        jary = toByteArray(javaBuf);
        Assert.assertEquals(dsry.length, jary.length);
        Assert.assertEquals(dsry, jary);
        //long
        javaBuf.putLong(12345);
        dsBuf.putLong(12345);
        dsry = dsBuf.toByteArray();
        jary = toByteArray(javaBuf);
        Assert.assertEquals(dsry.length, jary.length);
        Assert.assertEquals(dsry, jary);
        //short
        javaBuf.putShort((short) 123);
        dsBuf.putShort((short) 123);
        dsry = dsBuf.toByteArray();
        jary = toByteArray(javaBuf);
        Assert.assertEquals(dsry.length, jary.length);
        Assert.assertEquals(dsry, jary);
    }

    @Test
    public void theTest() throws Exception {
        DSByteBuffer buffer = new DSByteBuffer(5);
        Assert.assertTrue(buffer.available() == 0);
        byte[] tmp = new byte[]{1, 2, 3, 4, 5};
        buffer.put(tmp, 0, tmp.length);
        Assert.assertTrue(buffer.available() == tmp.length);
        int len = buffer.sendTo(tmp, 0, tmp.length);
        Assert.assertTrue(len == tmp.length);
        Assert.assertEquals(tmp, new byte[]{1, 2, 3, 4, 5});
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

    private byte[] toByteArray(ByteBuffer buf) {
        byte[] ret = new byte[buf.position()];
        buf.flip();
        buf.get(ret);
        buf.clear();
        return ret;
    }

}
