package org.iot.dsa.dslink;

import com.acuity.iot.dsa.dslink.sys.DSSysNode;
import com.acuity.iot.dsa.dslink.sys.profiler.SysProfiler;
import com.acuity.iot.dsa.dslink.sys.profiler.ThreadNode;
import com.acuity.iot.dsa.dslink.test.V1TestLink;
import org.iot.dsa.dslink.requester.SimpleInvokeHandler;
import org.iot.dsa.node.DSIObject;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSNode;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SysProfilerTest {

    private static boolean success = false;
    private V1TestLink link;

    @Test
    public void theTest() throws Exception {
        link = new V1TestLink(new DSMainNode());
        Thread t = new Thread(link, "DSLink Runner");
        t.start();
        link.getConnection().waitForConnection(5000);
        Assert.assertTrue(link.getConnection().isConnected());
        DSIRequester requester = link.getConnection().getRequester();
        SimpleInvokeHandler res = (SimpleInvokeHandler) requester.invoke(
                "/sys/" + DSSysNode.OPEN_PROFILER, null, new SimpleInvokeHandler());
        res.getUpdate(2000);

        DSSysNode sys = (DSSysNode) link.get(DSLink.SYS);
        DSIObject profobj = sys.get(DSSysNode.PROFILER);
        Assert.assertTrue(profobj instanceof SysProfiler);

        SysProfiler profiler = (SysProfiler) profobj;
        DSIObject threadobj = profiler.get("Thread");
        Assert.assertTrue(threadobj instanceof ThreadNode);

        final ThreadNode thread = (ThreadNode) threadobj;
        final DSInfo cpuTime = thread.getInfo("CurrentThreadCpuTime");
        Assert.assertTrue(cpuTime != null);
        success = false;
        thread.subscribe((event, node, child, data) -> {
            Assert.assertEquals(thread, node);
            Assert.assertEquals(cpuTime, child);
            Assert.assertTrue(child.isValue());
            Assert.assertTrue(child.getValue().toElement().isNumber());
            synchronized (SysProfilerTest.this) {
                success = true;
                SysProfilerTest.this.notifyAll();
            }
        }, DSNode.VALUE_CHANGED_EVENT, cpuTime);
        synchronized (this) {
            if (!success) {
                this.wait(6000);
            }
        }
        Assert.assertTrue(success);
    }

}
