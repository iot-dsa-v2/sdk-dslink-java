package org.iot.dsa.dslink;

import java.io.File;
import java.net.URL;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import org.iot.dsa.io.NodeDecoder;
import org.iot.dsa.io.json.JsonReader;
import org.iot.dsa.logging.DSLogHandler;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.security.DSKeys;
import org.iot.dsa.util.DSException;

/**
 * The root node of a DSLink node tree with two children: main and sys.  Main is the root
 * data or application node.  Sys contains various services such as the upstream
 * connection to the broker.
 * <p>
 * This node can and should be used as the main class for launching a link process.
 * <p>
 * This node can be subclassed for specialized purposes.  Testing uses a version that
 * creates a special transport for sending and receiving messages to itself.
 *
 * @author Aaron Hansen
 */
public class DSLink extends DSNode implements Runnable {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    static final String MAIN = "main";
    static final String SYS = "sys";

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////
    private String dsId;
    private DSKeys keys;
    private DSInfo main = getInfo(MAIN);
    private String name;
    private DSLinkOptions options;
    private Thread runThread;
    private DSInfo sys = getInfo(SYS);

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

    public DSLinkConnection getConnection() {
        return getSys().getConnection();
    }

    /**
     * Returns the unique id of the connection.  This is the link name + '-' + the hash of the
     * public key in base64.
     *
     * @return Never null, and url safe.
     */
    public String getDsId() {
        if (dsId == null) {
            StringBuilder buf = new StringBuilder();
            buf.append(getLinkName());
            buf.append('-');
            buf.append(getKeys().encodePublicHashDsId());
            dsId = buf.toString();
        }
        return dsId;
    }

    /**
     * Public / private keys of the link, used to prove identity with brokers.
     */
    public DSKeys getKeys() {
        return keys;
    }

    /**
     * As defined in dslink.json.
     */
    public String getLinkName() {
        return name;
    }

    public DSMainNode getMain() {
        return (DSMainNode) main.getNode();
    }

    public DSLinkOptions getOptions() {
        return options;
    }

    public DSSysNode getSys() {
        return (DSSysNode) sys.getNode();
    }

    /**
     * Creates a link by first testing for an existing serialized database.
     *
     * @param config Configuration options
     */
    public static DSLink load(DSLinkOptions config) {
        Logger logger = Logger.getLogger("");
        DSLink ret = null;
        File nodes = config.getNodesFile();
        if (nodes.exists()) {
            logger.info("Loading node database " + nodes.getAbsolutePath());
            long time = System.currentTimeMillis();
            JsonReader reader = new JsonReader(nodes);
            DSNode node = NodeDecoder.decode(reader);
            reader.close();
            if (node instanceof DSLink) {
                ret = (DSLink) node;
            } else {
                ret = new DSLink();
                ret.setNodes((DSMainNode) node);
            }
            ret.init(config);
            time = System.currentTimeMillis() - time;
            ret.info("Node database loaded: " + time + "ms");
        } else {
            ret = new DSLink();
            ret.init(config);
            ret.info("Creating new database...");
            String type = config.getMainType();
            if (type == null) {
                throw new IllegalStateException("Config missing the main node type");
            }
            ret.debug("Main type: " + type);
            try {
                DSNode node = (DSNode) Class.forName(type).newInstance();
                ret.put(MAIN, node);
            } catch (Exception x) {
                DSException.throwRuntime(x);
            }
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
                info(info() ? src : null);
            } catch (Throwable t) {
                debug("Reporting source of DSLink.class", t);
            }
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    info("Running shutdown hook");
                    shutdown();
                }
            });
            info(info() ? "Starting nodes" : null);
            start();
            long stableDelay = options.getConfig(DSLinkOptions.CFG_STABLE_DELAY, 5000l);
            try {
                Thread.sleep(stableDelay);
            } catch (Exception x) {
                debug("Interrupted stable delay", x);
            }
            try {
                info(info() ? "Stabilizing nodes" : null);
                stable();
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
            Logger logger = Logger.getLogger("");
            for (Handler h : logger.getLogger("").getHandlers()) {
                h.close();
            }
        } finally {
            runThread = null;
        }
    }

    public DSLink setNodes(DSMainNode node) {
        put(main, node);
        return this;
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

    /**
     * Adds the save action, overrides should call super if they want this action.
     */
    @Override
    protected void declareDefaults() {
        declareDefault(MAIN, new DSNode());
        declareDefault(SYS, new DSSysNode()).setAdmin(true);
    }

    @Override
    protected String getLogName() {
        String s = getLinkName();
        if ((s == null) || s.isEmpty()) {
            return getClass().getSimpleName();
        }
        if (s.startsWith("dslink-java")) {
            if (s.startsWith("dslink-java-v2-")) {
                s = s.substring("dslink-java-v2-".length());
            } else if (s.startsWith("dslink-java-")) {
                s = s.substring("dslink-java-".length());
            }
        }
        if ((s == null) || s.isEmpty()) {
            return getClass().getSimpleName();
        }
        return s;
    }

    /**
     * Configures a link instance including creating the appropriate connection.
     *
     * @return This
     */
    protected DSLink init(DSLinkOptions config) {
        this.options = config;
        DSLogHandler.setRootLevel(config.getLogLevel());
        name = config.getLinkName();
        keys = config.getKeys();
        getSys().init();
        return this;
    }

    @Override
    protected void onStopped() {
        synchronized (this) {
            notifyAll();
        }
    }

}
