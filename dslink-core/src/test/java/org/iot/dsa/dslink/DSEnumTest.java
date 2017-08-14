package org.iot.dsa.dslink;

import java.util.Iterator;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSEnum;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Aaron Hansen
 */
public class DSEnumTest {

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
        DSEnum javaEnum = DSEnum.valueOf(TestEnum.FIRST);
        DSElement e = javaEnum.encode();
        javaEnum = (DSEnum) DSEnum.NULL.decode(e);
        Assert.assertTrue(javaEnum.toString().equals("FIRST"));
        Iterator<String>  it = javaEnum.getEnums().iterator();
        Assert.assertTrue(it.next().equals("FIRST"));
        Assert.assertTrue(it.next().equals("SECOND"));
        Assert.assertTrue(it.next().equals("THIRD"));
    }

    // Inner Classes
    // -------------

    private enum TestEnum {
        FIRST,
        SECOND,
        THIRD
    }

}
