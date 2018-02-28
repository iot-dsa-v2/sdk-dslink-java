package org.iot.dsa.dslink;

import com.acuity.iot.dsa.dslink.transport.DSTransport;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import org.iot.dsa.io.json.JsonReader;
import org.iot.dsa.io.json.JsonWriter;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSElementType;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.time.DSTime;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Aaron Hansen
 */
public class DSElementTest {

    @Test
    public void tmp() throws Exception {
        System.out.println("max: " +  Long.MAX_VALUE);
        System.out.println("min: " +  Long.MAX_VALUE);
        long time = System.nanoTime();
        System.out.println("now: " +  time);
        time /= DSTime.NANOS_IN_MS;
        System.out.println("now millis: " +  time);
        time /= 60000;
        System.out.println("now minutes: " +  time);
        System.out.println("currentTimeMIllis: " +  System.currentTimeMillis());
        long tmp = Long.MAX_VALUE - 2;
        tmp += 3;
        System.out.println("rollover: " +  tmp);
    }

    @Test
    public void test() throws Exception {
        test(DSElement.make(true), true);
        test(DSElement.make(false), false);
        test(DSElement.make(0d), 0d);
        test(DSElement.make(100.5d), 100.5d);
        test(DSElement.make(0), 0);
        test(DSElement.make(101), 101);
        test(DSElement.make(0l), 0l);
        test(DSElement.make(101l), 101l);
        Assert.assertTrue(DSElement.make(101l).isLong());
        test(DSElement.make(""), "");
        test(DSElement.make("0"), "0");
        test(DSElement.make("abc"), "abc");
        testNull(DSElement.make((String) null));
        testNull(DSElement.makeNull());
        testMap();
    }

    private DSList primitiveList() {
        return new DSList()
                .add(true)
                .add(100.001d)
                .add(100001)
                .add(100001l)
                .add("abcdefghij\r\njklmnopqrs\u0000\u0001\u0002tuvwxyz\r\n")
                .addNull();
    }

    private DSMap primitiveMap() {
        return new DSMap()
                .put("boolean", true)
                .put("double", 105.001d)
                .put("int", 100001)
                .put("long", (long) 123l)
                .put("string", "abcdefghij\r\njklmnopqrs\u0000\u0001\u0002tuvwxyz\r\n")
                .putNull("null");
    }

    private void test(DSElement obj, boolean value) {
        Assert.assertTrue(obj.getElementType() == DSElementType.BOOLEAN);
        Assert.assertTrue(DSElement.make(value) == obj);
        Assert.assertTrue(obj.isBoolean());
        Assert.assertFalse(obj.isDouble());
        Assert.assertFalse(obj.isGroup());
        Assert.assertFalse(obj.isLong());
        Assert.assertFalse(obj.isList());
        Assert.assertFalse(obj.isMap());
        Assert.assertFalse(obj.isNull());
        Assert.assertFalse(obj.isNumber());
        Assert.assertFalse(obj.isString());
        Assert.assertTrue(obj.toBoolean() == value);
        int num = value ? 1 : 0;
        Assert.assertTrue(obj.toDouble() == num);
        Assert.assertTrue(obj.toFloat() == num);
        Assert.assertTrue(obj.toInt() == num);
        Assert.assertTrue(obj.toLong() == num);
        Assert.assertTrue(obj.toString().equals(value + ""));
    }

    private void test(DSElement obj, double value) {
        Assert.assertTrue(obj.getElementType() == DSElementType.DOUBLE);
        if (value < 100) {
            Assert.assertTrue(DSElement.make(value) == obj);
        }
        Assert.assertFalse(obj.isBoolean());
        Assert.assertTrue(obj.isDouble());
        Assert.assertFalse(obj.isGroup());
        Assert.assertFalse(obj.isLong());
        Assert.assertFalse(obj.isList());
        Assert.assertFalse(obj.isMap());
        Assert.assertFalse(obj.isNull());
        Assert.assertTrue(obj.isNumber());
        Assert.assertFalse(obj.isString());
        if (value == 0) {
            Assert.assertFalse(obj.toBoolean());
        } else {
            Assert.assertTrue(obj.toBoolean());
        }
        Assert.assertTrue(obj.toDouble() == value);
        Assert.assertTrue(obj.toFloat() == value);
    }

    private void test(DSElement obj, int value) {
        Assert.assertTrue(obj.getElementType() == DSElementType.LONG);
        if (value <= 100) {
            Assert.assertTrue(DSElement.make(value) == obj);
        }
        Assert.assertFalse(obj.isBoolean());
        Assert.assertFalse(obj.isDouble());
        Assert.assertFalse(obj.isGroup());
        Assert.assertTrue(obj.isLong());
        Assert.assertFalse(obj.isList());
        Assert.assertFalse(obj.isMap());
        Assert.assertFalse(obj.isNull());
        Assert.assertTrue(obj.isNumber());
        Assert.assertFalse(obj.isString());
        if (value == 0) {
            Assert.assertFalse(obj.toBoolean());
        } else {
            Assert.assertTrue(obj.toBoolean());
        }
        Assert.assertTrue(obj.toDouble() == value);
        Assert.assertTrue(obj.toFloat() == value);
        Assert.assertTrue(obj.toInt() == value);
        Assert.assertTrue(obj.toLong() == value);
    }

    private void test(DSElement obj, long value) {
        Assert.assertTrue(obj.getElementType() == DSElementType.LONG);
        if (value <= 100) {
            Assert.assertTrue(DSElement.make(value) == obj);
        }
        Assert.assertFalse(obj.isBoolean());
        Assert.assertFalse(obj.isDouble());
        Assert.assertFalse(obj.isGroup());
        Assert.assertTrue(obj.isLong());
        Assert.assertFalse(obj.isList());
        Assert.assertFalse(obj.isMap());
        Assert.assertFalse(obj.isNull());
        Assert.assertTrue(obj.isNumber());
        Assert.assertFalse(obj.isString());
        if (value == 0) {
            Assert.assertFalse(obj.toBoolean());
        } else {
            Assert.assertTrue(obj.toBoolean());
        }
        Assert.assertTrue(obj.toDouble() == value);
        Assert.assertTrue(obj.toFloat() == value);
        Assert.assertTrue(obj.toInt() == value);
        Assert.assertTrue(obj.toLong() == value);
    }

    private void test(DSElement obj, String value) {
        Assert.assertTrue(obj.getElementType() == DSElementType.STRING);
        if (value.isEmpty()) {
            Assert.assertTrue(DSElement.make("") == obj);
        }
        Assert.assertFalse(obj.isBoolean());
        Assert.assertFalse(obj.isDouble());
        Assert.assertFalse(obj.isGroup());
        Assert.assertFalse(obj.isLong());
        Assert.assertFalse(obj.isList());
        Assert.assertFalse(obj.isMap());
        Assert.assertFalse(obj.isNull());
        Assert.assertFalse(obj.isNumber());
        Assert.assertTrue(obj.isString());
        if (value.equals("0")) {
            Assert.assertFalse(obj.toBoolean());
        } else if (value.equals("1")) {
            Assert.assertTrue(obj.toBoolean());
        }
        Assert.assertTrue(obj.toString().equals(value));
    }

    private void testList(DSList list) {
        Assert.assertTrue(list.getElementType() == DSElementType.LIST);
        Assert.assertTrue(list.isList());
        int size = list.size();
        if (size == 0) {
            Assert.assertTrue(list.isEmpty());
        }
        if (list.isEmpty()) {
            Assert.assertTrue(size == 0);
        }
        DSElement tmp = DSElement.make("mustNotContain");
        list.add(tmp);
        Assert.assertFalse(list.isEmpty());
        Assert.assertTrue(list.size() == (size + 1));
        Assert.assertTrue(list.remove(size) == tmp);
        Assert.assertTrue(list.size() == size);
        boolean fail = true;
        try {
            tmp = new DSMap();
            list.add(tmp);
            list.add(tmp);
        } catch (Exception ignore) {
            fail = false;
        }
        if (fail) {
            throw new IllegalStateException("Parenting failure");
        }
        Assert.assertTrue(list.removeLast() == tmp);
    }

    private void testMap() {
        testMap(new DSMap());
        DSMap map = primitiveMap();
        testPrimitiveMap(map);
        DSList list = primitiveList();
        testPrimitiveList(list);
        map.put("map", primitiveMap());
        map.put("list", primitiveList());
        Assert.assertTrue(map.get(6).isMap());
        Assert.assertTrue(map.get(7).isList());
        testMap(map);
        testPrimitiveMap(map.getMap("map"));
        //encode and reconstitute
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new JsonWriter(out, "UTF-8").value(map).close();
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        map = new JsonReader(in, "UTF-8").getElement().toMap();
        Assert.assertTrue(map.get(6).isMap());
        Assert.assertTrue(map.get(7).isList());
        testPrimitiveMap(map.getMap("map"));
        list.add(primitiveMap());
        list.add(primitiveList());
        testList(list);
        Assert.assertTrue(list.get(6).isMap());
        Assert.assertTrue(list.get(7).isList());
        out = new ByteArrayOutputStream();
        new JsonWriter(out, "UTF-8").value(map).close();
        in = new ByteArrayInputStream(out.toByteArray());
        map = new JsonReader(in, "UTF-8").getElement().toMap();
        Assert.assertTrue(map.get(6).isMap());
        Assert.assertTrue(map.get(7).isList());
        testPrimitiveList(list.get(7).toList());
    }

    private void testMap(DSMap map) {
        Assert.assertTrue(map.getElementType() == DSElementType.MAP);
        Assert.assertTrue(map.isMap());
        int size = map.size();
        if (size == 0) {
            Assert.assertTrue(map.isEmpty());
        }
        if (map.isEmpty()) {
            Assert.assertTrue(size == 0);
        }
        Assert.assertTrue(map.get("mustNotContain", null) == null);
        Assert.assertTrue(map.isNull("mustNotContain"));
        map.put("mustNotContain", 10);
        Assert.assertFalse(map.isNull("mustNotContain"));
        Assert.assertFalse(map.isEmpty());
        Assert.assertTrue(map.size() == (size + 1));
        Assert.assertTrue(map.remove("mustNotContain").toInt() == 10);
        Assert.assertTrue(map.size() == size);
        map.putNull("mustNotContain");
        Assert.assertFalse(map.isEmpty());
        Assert.assertTrue(map.size() == (size + 1));
        Assert.assertTrue(map.remove("mustNotContain").isNull());
        Assert.assertTrue(map.size() == size);
        map.putNull("mustNotContain");
        map.put("mustNotContain", 10);
        Assert.assertTrue(map.size() == (size + 1));
        Assert.assertTrue(map.remove("mustNotContain").toInt() == 10);
        Assert.assertTrue(map.size() == size);
        boolean fail = true;
        DSMap tmp = new DSMap();
        try {
            map.put("mustNotContain", tmp);
            DSList list = new DSList();
            list.add(tmp);
        } catch (Exception ignore) {
            fail = false;
        }
        if (fail) {
            throw new IllegalStateException("Parenting failure");
        }
        Assert.assertTrue(map.removeLast() == tmp);
    }

    private void testPrimitiveList(DSList list) {
        testList(list);
        Assert.assertTrue(list.get(0).isBoolean());
        Assert.assertTrue(list.get(1).isDouble());
        Assert.assertTrue(list.get(2).isLong()); //Deserializes as a long
        Assert.assertTrue(list.get(3).isLong());
        Assert.assertTrue(list.get(4).isString());
        Assert.assertTrue(list.get(5).isNull());
    }

    private void testPrimitiveMap(DSMap map) {
        testMap(map);
        Assert.assertTrue(map.get(0).isBoolean());
        Assert.assertTrue(map.get(1).isDouble());
        Assert.assertTrue(map.get(2).isLong()); //Deserializes as a long
        Assert.assertTrue(map.get(3).isNumber());
        Assert.assertTrue(map.get(4).isString());
        Assert.assertTrue(map.get(5).isNull());
        Assert.assertTrue(map.get("boolean").isBoolean());
        Assert.assertTrue(map.get("double").isDouble());
        Assert.assertTrue(map.get("int").isLong()); //Deserializes as a long
        Assert.assertTrue(map.get("long").isLong());
        Assert.assertTrue(map.get("string").isString());
        Assert.assertTrue(map.get("null").isNull());
        Assert.assertTrue(map.indexOf("boolean") == 0);
        Assert.assertTrue(map.indexOf("double") == 1);
        Assert.assertTrue(map.indexOf("int") == 2);
        Assert.assertTrue(map.indexOf("long") == 3);
        Assert.assertTrue(map.indexOf("string") == 4);
        Assert.assertTrue(map.indexOf("null") == 5);
    }

    private void testNull(DSElement obj) {
        Assert.assertTrue(DSElement.makeNull() == obj);
        Assert.assertTrue(obj.isNull());
        Assert.assertTrue(obj.getElementType() == DSElementType.NULL);
    }

}
