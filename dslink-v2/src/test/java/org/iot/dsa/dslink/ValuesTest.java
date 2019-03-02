package org.iot.dsa.dslink;

import org.iot.dsa.node.DSBool;
import org.iot.dsa.node.DSBytes;
import org.iot.dsa.node.DSDouble;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSFlexEnum;
import org.iot.dsa.node.DSFloat;
import org.iot.dsa.node.DSIStorable;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSInt;
import org.iot.dsa.node.DSJavaEnum;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSLong;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSNull;
import org.iot.dsa.rollup.DSRollup;
import org.iot.dsa.time.DSDateTime;
import org.iot.dsa.time.DSDuration;
import org.iot.dsa.time.DSTimeRange;
import org.iot.dsa.time.DSTimezone;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ValuesTest {

    ///////////////////////////////////////////////////////////////////////////
    // Class Fields
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    @Test
    public void test() {
        test(DSBool.TRUE);
        test(DSBytes.valueOf(new byte[]{1, 2, 3}));
        test(DSDouble.valueOf(123124));
        test(DSFlexEnum.valueOf("foo", DSList.valueOf("foo", "bar")));
        test(DSFloat.valueOf(123124));
        test(DSInt.valueOf(123124));
        test(DSJavaEnum.valueOf(TestEnum.one));
        test(DSLong.valueOf(123124));
        test(DSList.valueOf("one", "two", "three"));
        test(new DSMap().put("one", 1).put("two", 2));
        test(DSNull.NULL);
        test(DSRollup.AND);
        test(DSDateTime.now());
        test(DSDuration.valueOf(false, 1, 1, 1));
        test(DSTimeRange.valueOf(DSDateTime.now(), DSDateTime.now().nextDay()));
        test(DSTimezone.DEFAULT);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected Methods
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Package Methods
    ///////////////////////////////////////////////////////////////////////////

    static void test(DSIValue v) {
        Assert.assertNotNull(v.getValueType());
        DSElement e = v.toElement();
        Assert.assertNotNull(e);
        DSIValue tmp = v.valueOf(e);
        Assert.assertEquals(v, tmp);
        if (v.isNull()) {
            Assert.assertTrue(tmp.isNull());
        } else {
            Assert.assertFalse(tmp.isNull());
        }
        Assert.assertEquals(v, v.copy());
        testElement(e);
        if (v instanceof DSIStorable) {
            DSIStorable s = (DSIStorable) v;
            e = s.store();
            tmp = s.restore(e);
            Assert.assertEquals(v, tmp);
        }
    }

    private static void testElement(DSElement e) {
        Assert.assertEquals(e, e.copy());
        switch (e.getElementType()) {
            case BOOLEAN:
                Assert.assertNotNull(e.toBoolean());
                break;
            case BYTES:
                Assert.assertNotNull(e.toBytes());
                break;
            case DOUBLE:
                Assert.assertNotNull(e.toDouble());
                break;
            case LIST:
                Assert.assertNotNull(e.toList());
                break;
            case LONG:
                Assert.assertNotNull(e.toLong());
                break;
            case MAP:
                Assert.assertNotNull(e.toMap());
                break;
            case NULL:
                Assert.assertTrue(e.isNull());
                break;
            case STRING:
                Assert.assertNotNull(e.toString());
                break;
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Private Methods
    ///////////////////////////////////////////////////////////////////////////

    private static enum TestEnum {
        one,
        two,
        three
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Initialization
    ///////////////////////////////////////////////////////////////////////////

}
