package org.iot.dsa.dslink;

import com.acuity.iot.dsa.dslink.io.msgpack.MsgpackReader;
import com.acuity.iot.dsa.dslink.io.msgpack.MsgpackWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import org.iot.dsa.io.DSIReader;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSString;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Aaron Hansen
 */
public class MsgpackTest {

    @Test
    public void testStrings() throws Exception {
        String input = "He wes Leovenaðes sone -- liðe him be Drihten.";
        CharBuffer charBuffer = CharBuffer.allocate(1024);
        charBuffer.append(input);
        charBuffer.flip();
        String result = charBuffer.toString();
        Assert.assertTrue(result.equals(input));
        //reuse the char buffer
        charBuffer.clear();
        charBuffer.append(input);
        charBuffer.flip();
        result = charBuffer.toString();
        Assert.assertTrue(result.equals(input));
        charBuffer.clear();
        charBuffer.append(input);
        charBuffer.flip();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        CharsetEncoder encoder = DSString.UTF8.newEncoder();
        encoder.encode(charBuffer, byteBuffer, false);
        byteBuffer.flip();
        CharsetDecoder decoder = DSString.UTF8.newDecoder();
        charBuffer.clear();
        decoder.decode(byteBuffer, charBuffer, false);
        charBuffer.flip();
        result = charBuffer.toString();
        Assert.assertTrue(result.equals(input));
    }

    @Test
    public void theTest() throws Exception {
        MsgpackWriter tmp = new MsgpackWriter();
        tmp.beginList();
        tmp.value("abc");
        tmp.endList();
        DSIReader reader = new MsgpackReader(new ByteArrayInputStream(tmp.toByteArray()));
        reader.getElement().toList();

        final ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
        MsgpackWriter out = new MsgpackWriter();
        out.beginList();
        out.value("abc");
        out.value(10);
        out.value(true);
        out.value(10.1d);
        out.value(new DSMap());
        out.value(new DSList());
        out.endList();
        //out.writeTo(baos1);
        //out.reset();
        byte[] encoded = out.toByteArray();
        DSIReader parser = new MsgpackReader(new ByteArrayInputStream(encoded));
        DSList list = parser.getElement().toList();
        parser.close();
        Assert.assertEquals(list.size(), 6);
        Assert.assertEquals(list.get(0).toString(), "abc");
        Assert.assertEquals(list.get(1).toInt(), 10);
        Assert.assertTrue(list.get(2).toBoolean());
        Assert.assertEquals(list.get(3).toDouble(), 10.1d, 0);
        final ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        out = new MsgpackWriter();
        out.beginMap();
        out.key("first").value("abc");
        out.key("second").value(10);
        out.key("third").value(true);
        out.key("fourth").value(10.1d);
        out.key("fifth").value(new DSMap());
        out.key("sixth");
        {
            out.beginList();
            out.value("abc");
            out.value(10);
            out.value(true);
            out.value(10.1d);
            out.value(new DSMap());
            out.value(new DSList());
            out.endList();
        }
        out.key("seventh").value("somebytes".getBytes());
        out.endMap();
        out.writeTo(baos2);
        out.reset();
        encoded = baos2.toByteArray();
        parser = new MsgpackReader(new ByteArrayInputStream(encoded));
        DSMap map = parser.getElement().toMap();
        Assert.assertEquals(map.size(), 7);
        Assert.assertEquals(map.get("first").toString(), "abc");
        Assert.assertTrue(map.get("third").toBoolean());
        Assert.assertEquals(map.get("fourth").toDouble(), 10.1d, 0);
        Assert.assertTrue(map.get("fifth").isMap());
        Assert.assertTrue(map.get("seventh").isBytes());
        Assert.assertEquals(
                new String(map.get("seventh").toBytes(), "UTF-8"), "somebytes");
        list = map.getList("sixth");
        Assert.assertEquals(list.size(), 6);
        Assert.assertEquals(list.get(0).toString(), "abc");
        Assert.assertEquals(list.get(1).toInt(), 10);
        Assert.assertTrue(list.get(2).toBoolean());
        Assert.assertEquals(list.get(3).toDouble(), 10.1d, 0);
    }

}
