package org.iot.dsa.dslink;

import com.acuity.iot.dsa.dslink.logging.AsyncLogHandler;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Calendar;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.iot.dsa.io.NodeDecoder;
import org.iot.dsa.io.NodeEncoder;
import org.iot.dsa.io.json.JsonReader;
import org.iot.dsa.io.json.JsonWriter;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.security.DSKeys;
import org.iot.dsa.time.DSTime;
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

    private DSLinkConfig config;
    private String dsId;
    private DSKeys keys;
    private DSInfo main = getInfo(MAIN);
    private String name;
    private Thread runThread;
    private boolean saveEnabled = true;
    private DSInfo sys = getInfo(SYS);

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Use the load method to create links.
     *
     * @see #load(DSLinkConfig)
     */
    public DSLink() {
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods in alphabetical order
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Adds the save action, overrides should call super if they want this action.
     */
    @Override
    protected void declareDefaults() {
        declareDefault(MAIN, new DSNode());
        declareDefault(SYS, new DSSysNode()).setAdmin(true);
    }

    public DSLinkConfig getConfig() {
        return config;
    }

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

    @Override
    protected String getLogName() {
        return getClass().getSimpleName();
    }

    public DSMainNode getMain() {
        return (DSMainNode) main.getNode();
    }

    public DSSysNode getSys() {
        return (DSSysNode) sys.getNode();
    }

    /**
     * Configures a link instance including creating the appropriate connection.
     *
     * @return This
     */
    protected DSLink init(DSLinkConfig config) {
        this.config = config;
        AsyncLogHandler.setRootLevel(config.getLogLevel());
        name = config.getLinkName();
        keys = config.getKeys();
        getSys().init();
        return this;
    }

    /**
     * Creates a link by first testing for an existing serialized database.
     *
     * @param config Configuration options
     */
    public static DSLink load(DSLinkConfig config) {
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
            ret.fine("Main type: " + type);
            try {
                DSNode node = (DSNode) Class.forName(type).newInstance();
                ret.put(MAIN, node);
            } catch (Exception x) {
                DSException.throwRuntime(x);
            }
            ret.save();
        }
        return ret;
    }

    /**
     * This is a convenience for DSLink.load(new DSLinkConfig(args)).run() and should be
     * used as the the main class for any link.  Use DSLink.shutdown() to stop running.
     */
    public static void main(String[] args) {
        try {
            DSLinkConfig cfg = new DSLinkConfig(args);
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
                warn("Reporting source of DSLink.class", t);
            }
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    info("Running shutdown hook");
                    shutdown();
                }
            });
            info(info() ? "Starting nodes" : null);
            start();
            long stableDelay = config.getConfig(DSLinkConfig.CFG_STABLE_DELAY, 5000l);
            try {
                Thread.sleep(stableDelay);
            } catch (Exception x) {
                warn("Interrupted stable delay", x);
            }
            try {
                info(info() ? "Stabilizing nodes" : null);
                stable();
                long saveInterval = config.getConfig(DSLinkConfig.CFG_SAVE_INTERVAL, 60);
                saveInterval *= 60000;
                long nextSave = System.currentTimeMillis() + saveInterval;
                while (isRunning()) {
                    synchronized (this) {
                        try {
                            wait(10000);
                        } catch (InterruptedException x) {
                            warn(getPath(), x);
                        }
                        if (System.currentTimeMillis() > nextSave) {
                            save();
                            nextSave = System.currentTimeMillis() + saveInterval;
                        }
                    }
                }
            } catch (Exception x) {
                error(getLinkName(), x);
                stop();
                DSException.throwRuntime(x);
            }
            save();
            LogManager.getLogManager().reset();
            Logger logger = Logger.getLogger("");
            for (Handler h : logger.getLogger("").getHandlers()) {
                h.close();
            }
        } finally {
            runThread = null;
        }
    }

    @Override
    protected void onStopped() {
        synchronized (this) {
            notifyAll();
        }
    }

    /**
     * Serializes the configuration database.
     */
    public void save() {
        if (!saveEnabled) {
            return;
        }
        ZipOutputStream zos = null;
        InputStream in = null;
        try {
            File nodes = config.getNodesFile();
            String name = nodes.getName();
            if (nodes.exists()) {
                info("Backing up the node database...");
                StringBuilder buf = new StringBuilder();
                Calendar cal = DSTime.getCalendar(System.currentTimeMillis());
                if (name.endsWith(".zip")) {
                    String tmp = name.substring(0, name.lastIndexOf(".zip"));
                    buf.append(tmp).append('.');
                    DSTime.encodeForFiles(cal, buf);
                    buf.append(".zip");
                    File bakFile = new File(nodes.getParent(), buf.toString());
                    nodes.renameTo(bakFile);
                } else {
                    buf.append(name).append('.');
                    DSTime.encodeForFiles(cal, buf);
                    buf.append(".zip");
                    File back = new File(nodes.getParent(), buf.toString());
                    FileOutputStream fos = new FileOutputStream(back);
                    zos = new ZipOutputStream(fos);
                    zos.putNextEntry(new ZipEntry(nodes.getName()));
                    byte[] b = new byte[4096];
                    in = new FileInputStream(nodes);
                    int len = in.read(b);
                    while (len > 0) {
                        zos.write(b, 0, len);
                        len = in.read(b);
                    }
                    in.close();
                    in = null;
                    zos.closeEntry();
                    zos.close();
                    zos = null;
                }
                DSTime.recycle(cal);
            }
            long time = System.currentTimeMillis();
            info("Saving node database " + nodes.getAbsolutePath());
            JsonWriter writer = null;
            if (name.endsWith(".zip")) {
                String tmp = name.substring(0, name.lastIndexOf(".zip"));
                writer = new JsonWriter(nodes, tmp + ".json");
            } else {
                writer = new JsonWriter(nodes);
            }
            NodeEncoder.encode(writer, this);
            writer.close();
            trimBackups();
            time = System.currentTimeMillis() - time;
            info("Node database saved: " + time + "ms");
        } catch (Exception x) {
            error("Saving node database", x);
        }
        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException x) {
            error("Closing input", x);
        }
        try {
            if (zos != null) {
                zos.close();
            }
        } catch (IOException x) {
            error("Closing output", x);
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
            fine(x);
        }
    }

    public DSLink setNodes(DSMainNode node) {
        put(main, node);
        return this;
    }

    /**
     * This is a transient option intended for unit tests. True by default.
     */
    protected DSLink setSaveEnabled(boolean enabled) {
        saveEnabled = enabled;
        return this;
    }

    /**
     * Called by save, no need to explicitly call.
     */
    private void trimBackups() {
        final File nodes = config.getNodesFile();
        if (nodes == null) {
            return;
        }
        final String nodesName = nodes.getName();
        final boolean isZip = nodesName.endsWith(".zip");
        int idx = nodesName.lastIndexOf('.');
        final String nameBase = nodesName.substring(0, idx);
        File dir = nodes.getAbsoluteFile().getParentFile();
        File[] backups = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                if (name.equals(nodesName)) {
                    return false;
                }
                if (isZip) {
                    if (name.endsWith(".zip")) {
                        return name.startsWith(nameBase);
                    }
                } else {
                    if (name.endsWith(".json")) {
                        return name.startsWith(nameBase);
                    }
                }
                return false;
            }
        });
        if (backups == null) {
            return;
        }
        Arrays.sort(backups);
        if (backups.length <= 3) {
            return;
        }
        for (int i = 0, len = backups.length - 3; i < len; i++) {
            backups[i].delete();
        }
    }

}
