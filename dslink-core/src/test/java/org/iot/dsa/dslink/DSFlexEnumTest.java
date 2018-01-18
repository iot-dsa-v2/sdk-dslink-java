package org.iot.dsa.dslink;

import java.util.Iterator;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSFlexEnum;
import org.iot.dsa.node.DSList;
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
        try {
            DSFlexEnum fe = DSFlexEnum.valueOf("First", DSList.valueOf("First", "Second", "Third"));
            Assert.assertTrue(fe.toString().equals("First"));
            Iterator<DSElement> it = fe.getEnums(null).iterator();
            Assert.assertTrue(it.next().toString().equals("First"));
            Assert.assertTrue(it.next().toString().equals("Second"));
            Assert.assertTrue(it.next().toString().equals("Third"));
            DSElement e = fe.store();
            fe = DSFlexEnum.NULL.restore(e);
            Assert.assertTrue(fe.toString().equals("First"));
            it = fe.getEnums(null).iterator();
            Assert.assertTrue(it.next().toString().equals("First"));
            Assert.assertTrue(it.next().toString().equals("Second"));
            Assert.assertTrue(it.next().toString().equals("Third"));
            fe = fe.valueOf("Second");
            Assert.assertTrue(fe.toString().equals("Second"));
            it = fe.getEnums(null).iterator();
            Assert.assertTrue(it.next().toString().equals("First"));
            Assert.assertTrue(it.next().toString().equals("Second"));
            Assert.assertTrue(it.next().toString().equals("Third"));
        } catch (Exception x) {
            x.printStackTrace();
        }
    }

    // Inner Classes
    // -------------

}
