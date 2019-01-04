package org.iot.dsa.dslink;

import com.acuity.iot.dsa.dslink.sys.profiler.SysProfiler;
import com.acuity.iot.dsa.dslink.sys.profiler.ThreadNode;
import com.acuity.iot.dsa.dslink.test.TestLink;
import org.iot.dsa.dslink.requester.SimpleInvokeHandler;
import org.iot.dsa.node.DSIObject;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSNode;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SysProfilerTest {

    private DSLink link;
    private static boolean success = false;

    @Test
    public void theTest() throws Exception {
        link = new TestLink(new DSMainNode());
        link.getConnection().subscribe(DSLinkConnection.CONNECTED, null, (node, child, event) -> {
            success = true;
            synchronized (SysProfilerTest.this) {
                SysProfilerTest.this.notifyAll();
            }
        });
        success = false;
        Thread t = new Thread(link, "DSLink Runner");
        t.start();
        synchronized (this) {
            this.wait(5000);
        }
        Assert.assertTrue(success);
        success = false;
        DSIRequester requester = link.getConnection().getRequester();
        SimpleInvokeHandler res = (SimpleInvokeHandler) requester.invoke(
                "/sys/" + DSSysNode.OPEN_PROFILER, null, new SimpleInvokeHandler());
        res.getUpdate(1000);

        DSSysNode sys = link.getSys();
        DSIObject profobj = sys.get(DSSysNode.PROFILER);
        Assert.assertTrue(profobj instanceof SysProfiler);

        SysProfiler profiler = (SysProfiler) profobj;
        DSIObject threadobj = profiler.get("Thread");
        Assert.assertTrue(threadobj instanceof ThreadNode);

        final ThreadNode thread = (ThreadNode) threadobj;
        final DSInfo cpuTime = thread.getInfo("CurrentThreadCpuTime");
        Assert.assertTrue(cpuTime != null);
        thread.subscribe(DSNode.VALUE_CHANGED, cpuTime, null, (node, child, event) -> {
            Assert.assertEquals(thread, node);
            Assert.assertEquals(cpuTime, child);
            Assert.assertTrue(child.isValue());
            Assert.assertTrue(child.getValue().toElement().isNumber());
            success = true;
            synchronized (SysProfilerTest.this) {
                SysProfilerTest.this.notifyAll();
            }
        });
        synchronized (this) {
            this.wait(6000);
        }
        Assert.assertTrue(success);
    }

}
