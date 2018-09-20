package org.iot.dsa.dslink;

import org.iot.dsa.node.DSPath;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Aaron Hansen
 */
public class DSPathTest {

    @Test
    public void testAttribute() throws Exception {
        String path = "/main/Node/@attr";
        String attr = null;
        int idx = 1 + path.lastIndexOf('/');
        if (path.charAt(idx) == '@') {
            String[] elems = DSPath.decodePath(path);
            idx = elems.length - 1;
            attr = elems[idx];
            path = DSPath.encodePath(path.charAt(0) == '/', elems, idx);
        }
        Assert.assertTrue(attr.equals("@attr"));
        Assert.assertTrue(path.equals("/main/Node"));
    }

}
