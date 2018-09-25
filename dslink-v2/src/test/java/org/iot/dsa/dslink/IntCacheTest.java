package org.iot.dsa.dslink;

import org.iot.dsa.node.DSInt;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Aaron Hansen
 */
public class IntCacheTest {

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
        DSInt dsint = DSInt.valueOf(0);
        Assert.assertTrue(DSInt.valueOf(0) == dsint);
        Assert.assertTrue(dsint.toInt() == 0);

        dsint = DSInt.valueOf(-128);
        Assert.assertTrue(DSInt.valueOf(-128) == dsint);
        Assert.assertTrue(dsint.toInt() == -128);

        dsint = DSInt.valueOf(127);
        Assert.assertTrue(DSInt.valueOf(127) == dsint);
        Assert.assertTrue(dsint.toInt() == 127);

        dsint = DSInt.valueOf(10);
        Assert.assertTrue(DSInt.valueOf(10) == dsint);
        Assert.assertTrue(dsint.toInt() == 10);

        dsint = DSInt.valueOf(-129);
        Assert.assertTrue(DSInt.valueOf(-129) != dsint);
        Assert.assertTrue(dsint.toInt() == -129);

        dsint = DSInt.valueOf(128);
        Assert.assertTrue(DSInt.valueOf(129) != dsint);
        Assert.assertTrue(dsint.toInt() == 128);
    }

}
