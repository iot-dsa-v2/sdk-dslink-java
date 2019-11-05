package org.iot.dsa.dslink;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.TimeUnit;
import org.iot.dsa.io.DSIReader;
import org.iot.dsa.io.DSIWriter;
import org.iot.dsa.io.NodeDecoder;
import org.iot.dsa.io.NodeEncoder;
import org.iot.dsa.io.json.Json;
import org.iot.dsa.node.DSBool;
import org.iot.dsa.node.DSDouble;
import org.iot.dsa.node.DSFloat;
import org.iot.dsa.node.DSInt;
import org.iot.dsa.node.DSLong;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.DSString;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import org.testng.Assert;
import org.testng.annotations.Test;

public class JmhTest {

    ///////////////////////////////////////////////////////////////////////////
    // Class Fields
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    @Benchmark
    public void delement() {
        DSNode node = makeDelementTree();
        DSNode decoded = decode(encode(node));
        Assert.assertTrue(node.equivalent(decoded));
    }

    //@Test
    public void runBenchmark() throws Exception {
        Options opt = new OptionsBuilder()
                // Specify which benchmarks to run.
                // You can be more specific if you'd like to run only one benchmark per test.
                .include(this.getClass().getName() + ".*")
                // Set the following options as needed
                .mode(Mode.AverageTime)
                .timeUnit(TimeUnit.SECONDS)
                .warmupTime(TimeValue.seconds(2))
                .warmupIterations(3)
                .measurementTime(TimeValue.seconds(2))
                .measurementIterations(3)
                .threads(2)
                .forks(2)
                .shouldFailOnError(true)
                .shouldDoGC(true)
                //.jvmArgs("-XX:+UnlockDiagnosticVMOptions", "-XX:+PrintInlining")
                //.addProfiler(WinPerfAsmProfiler.class)
                .build();
        new Runner(opt).run();
    }

    /*
    @Benchmark
    public void pojo() {
        DSNode node = makePojoTree();
        DSNode decoded = decode(encode(node));
        Assert.assertTrue(node.equivalent(decoded));
    }
    */

    ///////////////////////////////////////////////////////////////////////////
    // Protected Methods
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Package Methods
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Private Methods
    ///////////////////////////////////////////////////////////////////////////

    private static DSNode decode(byte[] bytes) {
        ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
        DSIReader reader = Json.reader(bin);
        DSNode ret = NodeDecoder.decode(reader);
        reader.close();
        return ret;
    }

    private static byte[] encode(DSNode node) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DSIWriter writer = Json.writer(bos);
        NodeEncoder.encode(writer, node);
        writer.close();
        return bos.toByteArray();
    }

    private static DSNode makeDelement() {
        DSNode node = new DSNode();
        node.put("int", DSInt.valueOf(12345));
        node.put("float", DSFloat.valueOf(12345));
        node.put("double", DSDouble.valueOf(12345));
        node.put("long", DSLong.valueOf(12345));
        node.put("boolean", DSBool.FALSE);
        node.put("String", DSString.valueOf("abc"));
        return node;
    }

    private static DSNode makeDelementTree() {
        DSNode ret = makeDelement();
        for (int i = 0; i < 100; i++) {
            DSNode level1 = makeDelement();
            ret.add("level1_" + i, level1);
            for (int j = 0; j < 100; j++) {
                DSNode level2 = makeDelement();
                level1.add("level2_" + j, level2);
            }
        }
        return ret;
    }

    /*
    private static DSNode makePojo() {
        DSNode node = new DSNode();
        node.put("int", Integer.valueOf(12345));
        node.put("float", Float.valueOf(12345));
        node.put("double", Double.valueOf(12345));
        node.put("long", Long.valueOf(12345));
        node.put("boolean", Boolean.FALSE);
        node.put("String", "abc");
        return node;
    }

    private static DSNode makePojoTree() {
        DSNode ret = makePojo();
        for (int i = 0; i < 100; i++) {
            DSNode level1 = makePojo();
            ret.add("level1_" + i, level1);
            for (int j = 0; j < 100; j++) {
                DSNode level2 = makePojo();
                level1.add("level2_" + j, level2);
            }
        }
        return ret;
    }
    */

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Initialization
    ///////////////////////////////////////////////////////////////////////////

}
