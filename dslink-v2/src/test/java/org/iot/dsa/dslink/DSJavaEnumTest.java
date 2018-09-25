package org.iot.dsa.dslink;

import java.util.Iterator;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSJavaEnum;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Aaron Hansen
 */
public class DSJavaEnumTest {

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
        DSJavaEnum javaEnum = DSJavaEnum.valueOf(TestEnum.FIRST);
        DSElement e = javaEnum.store();
        javaEnum = DSJavaEnum.NULL.restore(e);
        Assert.assertTrue(javaEnum.toString().equals("FIRST"));
        Iterator<DSElement> it = javaEnum.getEnums(null).iterator();
        Assert.assertTrue(it.next().toString().equals("FIRST"));
        Assert.assertTrue(it.next().toString().equals("SECOND"));
        Assert.assertTrue(it.next().toString().equals("THIRD"));
        javaEnum = javaEnum.valueOf("SECOND");
        it = javaEnum.getEnums(null).iterator();
        Assert.assertTrue(javaEnum.toString().equals("SECOND"));
        Assert.assertTrue(it.next().toString().equals("FIRST"));
        Assert.assertTrue(it.next().toString().equals("SECOND"));
        Assert.assertTrue(it.next().toString().equals("THIRD"));
    }

    // Inner Classes
    // -------------

    private enum TestEnum {
        FIRST,
        SECOND,
        THIRD
    }

}
