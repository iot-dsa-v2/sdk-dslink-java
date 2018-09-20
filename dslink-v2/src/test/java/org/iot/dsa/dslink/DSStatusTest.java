package org.iot.dsa.dslink;

import org.iot.dsa.node.DSStatus;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Aaron Hansen
 */
public class DSStatusTest {

    @Test
    public void test() {
        DSStatus status = DSStatus.valueOf(DSStatus.DOWN, DSStatus.FAULT);
        Assert.assertTrue(status.isFault());
        Assert.assertTrue(status.isAnyFault());
        Assert.assertTrue(status.isDown());
        Assert.assertTrue(status.isAnyDown());
        status = status.remove(DSStatus.DOWN);
        Assert.assertTrue(status.isFault());
        Assert.assertTrue(status.isAnyFault());
        Assert.assertFalse(status.isDown());
        Assert.assertFalse(status.isAnyDown());
        status = status.add(DSStatus.REMOTE_DISABLED);
        Assert.assertTrue(status.isFault());
        Assert.assertTrue(status.isAnyFault());
        Assert.assertTrue(status.isRemoteDisabled());
        Assert.assertTrue(status.isAnyDisabled());
    }

}
