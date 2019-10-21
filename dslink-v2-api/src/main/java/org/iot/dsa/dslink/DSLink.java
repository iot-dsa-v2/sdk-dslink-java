package org.iot.dsa.dslink;

import java.io.File;
import java.net.URL;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import org.iot.dsa.io.DSIReader;
import org.iot.dsa.io.NodeDecoder;
import org.iot.dsa.io.json.Json;
import org.iot.dsa.logging.DSLogger;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.DSPath;
import org.iot.dsa.node.DSString;
import org.iot.dsa.util.DSException;
import org.iot.dsa.util.DSUtil;

/**
 * Every problem is a link.  A link represents a single module with a specific purpose
 * in a system.  Links can be run in process and out of process.  They can be the tree root
 * or them can be found lower in the tree (such as representing out of process links in a
 * broker).  A  broker is also a link.
 * <p>
 * This node can be used as the main class for launching a link process.  It loads everything it
 * needs from dslink.json.
 * <p>
 * This node will be subclassed for specialized purposes.  Testing uses a version that
 * creates a special transport for sending and receiving messages to itself.
 *
 * @author Aaron Hansen
 */
public abstract class DSLink extends DSNode implements Runnable {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    public static final String DOWNSTREAM = "downstream";
    public static final String LINKID = "Link ID";
    public static final String MAIN = "main";
    public static final String SYS = "sys";
    public static final String UPSTREAM = "upstream";

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private String name;
    private DSLinkOptions options;
    private Thread runThread;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Use the load method to create links.
     *
     * @see #load(DSLinkOptions)
     */
    public DSLink() {
    }

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    public abstract DSLinkConnection getConnection();

    /**
     * Unique ID of the link instance.
     */
    public String getDsId() {
        String id = get(LINKID).toString();
        if (id.isEmpty()) {
            id = getLinkName() + '-' + options.getKeys().encodePublicHashDsId();
            put(LINKID, id);
        }
        return id;
    }

    /**
     * As defined in dslink.json.
     */
    public String getLinkName() {
        return name;
    }

    /**
     * The node that encapsulates the primary logic of the link.
     */
    public abstract DSMainNode getMain();

    public DSLinkOptions getOptions() {
        return options;
    }

    /**
     * The local path of the node appended the link's path in the upstream broker.
     */
    public String getPathInBroker(DSNode node) {
        String pathInBroker = getConnection().getPathInBroker();
        String nodePath = node.getPath();
        if ((pathInBroker == null) || pathInBroker.isEmpty()) {
            return nodePath;
        }
        StringBuilder buf = new StringBuilder(pathInBroker.length() + nodePath.length() + 10);
        buf.append(pathInBroker);
        return DSPath.append(buf, nodePath).toString();
    }

    /**
     * Creates a link by first testing for an existing serialized database.
     *
     * @param config Configuration options
     */
    public static DSLink load(DSLinkOptions config) {
        DSLink ret;
        File nodes = config.getNodesFile();
        if (nodes.exists()) {
            long time = System.currentTimeMillis();
            DSIReader reader = Json.reader(nodes);
            ret = (DSLink) NodeDecoder.decode(reader);
            reader.close();
            ret.init(config);
            time = System.currentTimeMillis() - time;
            ret.info("Database loaded: " + time + "ms");
        } else {
            String type = config.getConfig("linkType", null);
            if (type == null) {
                ret = (DSLink) DSUtil.newInstance(
                        "com.acuity.iot.dsa.dslink.protocol.DSRootLink");
            } else {
                ret = (DSLink) DSUtil.newInstance(type);
            }
            ret.debug("Creating new node database...");
            ret.init(config);
        }
        return ret;
    }

    /**
     * This is a convenience for DSLink.load(new DSLinkOptions(args)).run() and should be
     * used as the the main class for any link.  Use DSLink.shutdown() to stop running.
     */
    public static void main(String[] args) {
        try {
            DSLinkOptions cfg = new DSLinkOptions(args);
            DSLink link = DSLink.load(cfg);
            link.run();
        } catch (Exception x) {
            x.printStackTrace();
        }
    }

    /**
     * Calls starts, waits the stableDelay, then calls stable.  Does not return until
     * this node is stopped.
     */
    public void run() {
        synchronized (this) {
            if (runThread != null) {
                throw new IllegalStateException("Already running.");
            }
            runThread = Thread.currentThread();
        }
        try {
            Class clazz = DSLink.class;
            try {
                URL src = clazz.getProtectionDomain().getCodeSource().getLocation();
                info(src);
            } catch (Throwable t) {
                debug(null, t);
            }
            Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdown()));
            start();
            long stableDelay = options.getConfig(DSLinkOptions.CFG_STABLE_DELAY, 2000L);
            try {
                Thread.sleep(stableDelay);
            } catch (Exception x) {
                debug(null, x);
            }
            try {
                stable();
                info("Running");
                while (isRunning()) {
                    synchronized (this) {
                        try {
                            wait(10000);
                        } catch (InterruptedException x) {
                            debug(getPath(), x);
                        }
                    }
                }
            } catch (Exception x) {
                error(getLinkName(), x);
                stop();
                DSException.throwRuntime(x);
            }
            LogManager.getLogManager().reset();
            for (Handler h : Logger.getLogger("").getHandlers()) {
                h.close();
            }
        } finally {
            runThread = null;
        }
    }

    /**
     * Properly shuts down the link when a thread is executing the run method.
     */
    public void shutdown() {
        stop();
        Thread thread = runThread;
        if (thread == null) {
            return;
        }
        try {
            synchronized (thread) {
                thread.join();
            }
        } catch (Exception x) {
            debug(x);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected void declareDefaults() {
        super.declareDefaults();
        declareDefault(LINKID, DSString.EMPTY, "Unique instance ID.").setReadOnly(true);
    }

    /**
     * Override point, default is the MAIN constant.
     */
    protected String getMainName() {
        return MAIN;
    }

    /**
     * Called whether the link is deserialized or created new.
     *
     * @return This
     */
    protected DSLink init(DSLinkOptions config) {
        this.options = config;
        DSLogger.DSA.setLevel(config.getLogLevel());
        name = config.getLinkName();
        return this;
    }

    @Override
    protected void onStopped() {
        synchronized (this) {
            notifyAll();
        }
    }

    protected DSLink setMain(DSMainNode node) {
        put(MAIN, node);
        return this;
    }

}
