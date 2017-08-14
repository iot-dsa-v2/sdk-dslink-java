package org.iot.dsa.dslink;

import java.util.Iterator;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSFlexEnum;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Aaron Hansen
 */
public class DSFlexEnumTest {

    // Constants
    // ---------

    // Fields
    // ------

    // Constructors
    // ------------

    // Methods
    // -------

    @Test
    public void theTest() throws Exception {
        DSFlexEnum fe = DSFlexEnum.valueOf("First", "Second", "Third");
        Assert.assertTrue(fe.toString().equals("First"));
        Iterator<String>  it = fe.getEnums().iterator();
        Assert.assertTrue(it.next().equals("First"));
        Assert.assertTrue(it.next().equals("Second"));
        Assert.assertTrue(it.next().equals("Third"));
        DSElement e = fe.encode();
        fe = DSFlexEnum.NULL.decode(e);
        Assert.assertTrue(fe.toString().equals("First"));
        it = fe.getEnums().iterator();
        Assert.assertTrue(it.next().equals("First"));
        Assert.assertTrue(it.next().equals("Second"));
        Assert.assertTrue(it.next().equals("Third"));
        fe.add("Fourth");
        fe.setValue("Second");
        Assert.assertTrue(fe.toString().equals("Second"));
        e = fe.encode();
        fe = DSFlexEnum.NULL.decode(e);
        Assert.assertTrue(fe.toString().equals("Second"));
        it = fe.getEnums().iterator();
        Assert.assertTrue(it.next().equals("First"));
        Assert.assertTrue(it.next().equals("Second"));
        Assert.assertTrue(it.next().equals("Third"));
        Assert.assertTrue(it.next().equals("Fourth"));
    }


    // Inner Classes
    // -------------

}
