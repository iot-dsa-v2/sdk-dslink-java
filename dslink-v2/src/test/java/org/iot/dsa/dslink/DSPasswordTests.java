package org.iot.dsa.dslink;

import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSString;
import org.iot.dsa.security.DSPasswordAes256;
import org.iot.dsa.security.DSPasswordSha256;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Aaron Hansen
 */
public class DSPasswordTests {

    // Constants
    // ---------

    // Fields
    // ------

    // Constructors
    // ------------

    // Methods
    // -------

    @Test
    public void testAes256() {
        DSPasswordAes256 pass = DSPasswordAes256.valueOf("myPass");
        String encrypted = pass.toString();
        Assert.assertFalse(pass.toString().equals("myPass"));
        Assert.assertTrue(pass.decode().equals("myPass"));
        Assert.assertTrue(pass.isValid(DSString.valueOf("myPass")));
        Assert.assertFalse(pass.isValid(DSString.valueOf("asdf")));
        DSElement e = pass.store();
        pass = DSPasswordAes256.NULL.restore(e);
        Assert.assertFalse(pass.toString().equals("myPass"));
        Assert.assertTrue(pass.decode().equals("myPass"));
        Assert.assertTrue(pass.toString().equals(encrypted));
        Assert.assertTrue(pass.isValid(DSString.valueOf("myPass")));
        Assert.assertFalse(pass.isValid(DSString.valueOf("asdf")));
    }

    @Test
    public void testSha256() {
        DSPasswordSha256 pass = DSPasswordSha256.valueOf("myPass");
        String encrypted = pass.toString();
        Assert.assertFalse(pass.toString().equals("myPass"));
        Assert.assertTrue(pass.isValid(DSString.valueOf("myPass")));
        Assert.assertFalse(pass.isValid(DSString.valueOf("asdf")));
        DSElement e = pass.store();
        pass = DSPasswordSha256.NULL.restore(e);
        Assert.assertFalse(pass.toString().equals("myPass"));
        Assert.assertTrue(pass.toString().equals(encrypted));
        Assert.assertTrue(pass.isValid(DSString.valueOf("myPass")));
        Assert.assertFalse(pass.isValid(DSString.valueOf("asdf")));
    }

    // Inner Classes
    // -------------

}
