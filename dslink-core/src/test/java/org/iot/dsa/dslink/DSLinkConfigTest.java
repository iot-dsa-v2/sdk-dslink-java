package org.iot.dsa.dslink;

import java.io.File;
import org.iot.dsa.io.json.JsonWriter;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Aaron Hansen
 */
public class DSLinkConfigTest {

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
    public void test() throws Exception {
        File file = new File("dslink.json");
        boolean created = false;
        if (!file.exists()) {
            created = true;
            new JsonWriter(file).beginMap().endMap().flush().close();
        }
        DSLinkConfig args = new DSLinkConfig("--broker http://test");
        Assert.assertTrue(args.getBrokerUri().equals("http://test"));
        args = new DSLinkConfig("-b http://test");
        Assert.assertTrue(args.getBrokerUri().equals("http://test"));
        args = new DSLinkConfig("-b=http://test");
        Assert.assertTrue(args.getBrokerUri().equals("http://test"));
        args = new DSLinkConfig("--broker=http://test -h");
        Assert.assertTrue(args.getBrokerUri().equals("http://test"));
        Assert.assertTrue(args.wasHelpRequested());
        args = new DSLinkConfig("--log info --token abc");
        Assert.assertTrue(args.getToken().equals("abc"));
        args = new DSLinkConfig("--log=info --token=abc");
        Assert.assertTrue(args.getToken().equals("abc"));
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
