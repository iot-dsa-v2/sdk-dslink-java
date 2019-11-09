package org.iot.dsa.dslink;

import java.io.File;
import org.iot.dsa.io.json.Json;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Aaron Hansen
 */
public class DSLinkOptionsTest {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    @Test
    public void test() {
        File file = new File("dslink.json");
        boolean created = false;
        if (!file.exists()) {
            created = true;
            Json.writer(file).beginMap().endMap().flush().close();
        }
        DSLinkOptions args = new DSLinkOptions("--broker http://test");
        Assert.assertTrue(args.getBrokerUri().equals("http://test"));
        args = new DSLinkOptions("--broker=http://test");
        Assert.assertTrue(args.getBrokerUri().equals("http://test"));
        args = new DSLinkOptions("--broker=http://test -h");
        Assert.assertTrue(args.getBrokerUri().equals("http://test"));
        Assert.assertTrue(args.wasHelpRequested());
        args = new DSLinkOptions("--log info --token abc");
        Assert.assertTrue(args.getToken().equals("abc"));
        args = new DSLinkOptions("--log=info --token=abc");
        Assert.assertTrue(args.getToken().equals("abc"));
        args = new DSLinkOptions("--msgpack false");
        Assert.assertFalse(args.getMsgpack());
        if (created) {
            file.delete();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Initialization
    ///////////////////////////////////////////////////////////////////////////

}
