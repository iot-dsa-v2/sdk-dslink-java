package org.iot.dsa.dslink;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import org.iot.dsa.io.DSIReader;
import org.iot.dsa.io.DSIWriter;
import org.iot.dsa.io.msgpack.MsgpackReader;
import org.iot.dsa.io.msgpack.MsgpackWriter;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSString;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Aaron Hansen
 */
public class MsgpackTest {

    // Constants
    // ---------

    // Fields
    // ------

    // Constructors
    // ------------

    // Methods
    // -------

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
        final ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
        DSIWriter out = new MsgpackWriter() {
            @Override
            public DSIWriter flush() {
                writeTo(baos1);
                return this;
            }

            @Override
            public void close() {
                flush();
            }
        };
        out.beginList();
        out.value("abc");
        out.value(10);
        out.value(true);
        out.value(10.1d);
        out.value(new DSMap());
        out.value(new DSList());
        out.endList();
        out.reset();
        byte[] encoded = baos1.toByteArray();
        DSIReader parser = new MsgpackReader(new ByteArrayInputStream(encoded));
        DSList list = parser.getElement().toList();
        parser.close();
        Assert.assertTrue(list.size() == 6);
        Assert.assertTrue(list.get(0).toString().equals("abc"));
        Assert.assertTrue(list.get(1).toInt() == 10);
        Assert.assertTrue(list.get(2).toBoolean());
        Assert.assertTrue(list.get(3).toDouble() == 10.1d);
        final ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        out = new MsgpackWriter() {
            @Override
            public DSIWriter flush() {
                writeTo(baos2);
                return this;
            }

            @Override
            public void close() {
                flush();
            }
        };
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
        out.reset();
        encoded = baos2.toByteArray();
        parser = new MsgpackReader(new ByteArrayInputStream(encoded));
        DSMap map = parser.getElement().toMap();
        Assert.assertTrue(map.size() == 7);
        Assert.assertTrue(map.get("first").toString().equals("abc"));
        Assert.assertTrue(map.get(1).toInt() == 10);
        Assert.assertTrue(map.get("third").toBoolean());
        Assert.assertTrue(map.get("fourth").toDouble() == 10.1d);
        Assert.assertTrue(map.get("fifth").isMap());
        Assert.assertTrue(map.get(5).isList());
        Assert.assertTrue(map.get("seventh").isBytes());
        Assert.assertTrue(
                new String(map.get("seventh").toBytes(), "UTF-8").equals("somebytes"));
        list = map.getList(5);
        Assert.assertTrue(list.size() == 6);
        Assert.assertTrue(list.get(0).toString().equals("abc"));
        Assert.assertTrue(list.get(1).toInt() == 10);
        Assert.assertTrue(list.get(2).toBoolean());
        Assert.assertTrue(list.get(3).toDouble() == 10.1d);
    }

}
