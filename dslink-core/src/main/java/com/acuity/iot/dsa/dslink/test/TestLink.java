package com.acuity.iot.dsa.dslink.test;

import org.iot.dsa.dslink.DSMainNode;

/**
 * Routes requests and responses back to self.
 *
 * @author Aaron Hansen
 */
public class TestLink extends V1TestLink {

    public TestLink() {
    }

    public TestLink(DSMainNode mainNode) {
        super(mainNode);
    }

}
