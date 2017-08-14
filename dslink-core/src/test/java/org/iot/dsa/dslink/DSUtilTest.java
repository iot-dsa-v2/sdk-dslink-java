package org.iot.dsa.dslink;

import org.iot.dsa.util.DSUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Aaron Hansen
 */
public class DSUtilTest {

    // Constants
    // ---------

    // Fields
    // ------

    // Constructors
    // ------------

    // Methods
    // -------

    @Test
    public void test() throws Exception {
        Assert.assertTrue(DSUtil.equal(null, null));
        Assert.assertTrue(DSUtil.equal("abc", "abc"));
        Assert.assertFalse(DSUtil.equal(null, "abc"));
        Assert.assertFalse(DSUtil.equal("abc", null));
        int bits = 1;
        Assert.assertTrue(DSUtil.getBit(bits, 0));
        bits = DSUtil.setBit(bits, 0, false);
        Assert.assertFalse(DSUtil.getBit(bits, 0));
        bits = DSUtil.setBit(bits, 31, true);
        for (int i = 0; i < 31; i++) {
            Assert.assertFalse(DSUtil.getBit(bits, i));
        }
        Assert.assertTrue(DSUtil.getBit(bits, 31));
    }

}
