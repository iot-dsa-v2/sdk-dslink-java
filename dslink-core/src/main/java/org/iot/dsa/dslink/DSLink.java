package org.iot.dsa.dslink;

import com.acuity.iot.dsa.dslink.DSConnection;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.logging.Logger;
import org.iot.dsa.io.NodeDecoder;
import org.iot.dsa.io.NodeEncoder;
import org.iot.dsa.io.json.JsonReader;
import org.iot.dsa.io.json.JsonWriter;
import org.iot.dsa.logging.DSLogging;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.security.DSKeys;
import org.iot.dsa.time.DSTime;
import org.iot.dsa.util.DSException;

/**
 * Represents an upstream connection, a node hierarchy, and manages the lifecycle of both.
 *
 * <p>
 *
 * Links are created with DSLinkConfig object. The main method of the process is responsible for
 * creating the config.  After instantiation, the link should call DSLink.run()
 *
 * <p>
 *
 * Lifecycle:
 *
 * TODO
 *
 * @author Aaron Hansen
 */
public class DSLink extends DSNode {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSLinkConfig config;
    private DSLinkConnection connection;
    private String dsId;
    private DSKeys keys;
    private Logger logger;
    private String name;
    private DSRequester requester;
    private DSNode root;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public DSLink() {
    }

    /**
     * Loads the root node and creates the connection, this constructor can take a long time to
     * complete, and will throw an exception if anything goes wrong.  This does not call the start
     * method.
     *
     * @param config Make sure the required configs are set.
     * @throws Exception If absolutely anything is wrong.
     */
    public DSLink(DSLinkConfig config) throws Exception {
        this.config = config;
        DSLogging.replaceRootHandler();
        DSLogging.setDefaultFile(config.getLogFile());
        DSLogging.setDefaultLevel(config.getLogLevel());
        name = config.getLinkName();
        keys = config.getKeys();
        logger = DSLogging.getLogger(name);
        DSLogging.setDefaultLogger(logger);
        File nodes = config.getNodesFile();
        if (nodes.exists()) {
            info(info() ? "Loading node database..." : null);
            JsonReader reader = new JsonReader(nodes);
            root = NodeDecoder.decode(reader);
            reader.close();
            info(info() ? "Node database loaded" : null);
        } else {
            info(info() ? "Creating new database..." : null);
            String type = config.getConfig(DSLinkConfig.CFG_ROOT_TYPE, null);
            if (type == null) {
                throw new IllegalStateException("Config missing the root node type");
            }
            config(config() ? "Root type: " + type : null);
            DSNode tmp = (DSNode) Class.forName(type).newInstance();
            if (!(tmp instanceof DSResponder)) {
                throw new IllegalStateException("Root type not a responder: " + type);
            }
            root = tmp;
            saveNodes();
        }
        if (root instanceof DSRequester) {
            requester = (DSRequester) root;
        }
        add("Root", root).setTransient(true);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods in alphabetical order
    ///////////////////////////////////////////////////////////////////////////

    public DSLinkConfig getConfig() {
        return config;
    }

    public DSLinkConnection getConnection() {
        return connection;
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

    /**
     * The logger as defined in dslink.json.
     */
    @Override
    public Logger getLogger() {
        return logger;
    }

    /**
     * Returns the root node if it is a responder, otherwise null.
     */
    public DSResponder getResponder() {
        if (root instanceof DSResponder) {
            return (DSResponder) root;
        }
        return null;
    }

    public DSRequester getRequester() {
        return requester;
    }

    /**
     * Calls starts, waits the stableDelay, then calls stable.  Does not return until this node is
     * stopped.
     */
    public void run() {
        info(info() ? "Starting root node" : null);
        start();
        long stableDelay = config.getConfig(DSLinkConfig.CFG_STABLE_DELAY, 5000l);
        try {
            Thread.sleep(stableDelay);
        } catch (Exception x) {
            warn("Interrupted", x);
        }
        try {
            info(info() ? "Stabilizing root node" : null);
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
                        saveNodes();
                        nextSave = System.currentTimeMillis() + saveInterval;
                    }
                }
            }
        } catch (Exception x) {
            severe(getLinkName(), x);
            stop();
            DSException.throwRuntime(x);
        }
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                saveNodes();
            }
        });
    }

    /**
     * Creates a connection using the config object.
     */
    @Override
    protected void onStarted() {
        try {
            String type = config.getConfig(DSLinkConfig.CFG_CONNECTION_TYPE, null);
            if (type != null) {
                config(config() ? "Connection type: " + type : null);
                connection = (DSLinkConnection) Class.forName(type).newInstance();
            } else {
                connection = new DSConnection();
            }
            connection.setLink(this);
            add("Connection", connection);
        } catch (Exception x) {
            DSException.throwRuntime(x);
        }
    }

    @Override
    protected void onStopped() {
        synchronized (this) {
            notifyAll();
        }
    }

    /**
     * Serializes the node tree.
     */
    public void saveNodes() {
        try {
            File nodes = config.getNodesFile();
            StringBuilder buf = new StringBuilder();
            buf.append(nodes.getName()).append('.');
            Calendar cal = DSTime.getCalendar(System.currentTimeMillis());
            DSTime.encodeForFiles(cal, buf);
            DSTime.recycle(cal);
            buf.append(".zip");
            File back = new File(nodes.getParent(), buf.toString());
            nodes.renameTo(back);
            info("Saving node database");
            JsonWriter writer = new JsonWriter(nodes);
            NodeEncoder.encode(writer, root);
            writer.close();
            trimBackups();
            info("Node database saved");
        } catch (Exception x) {
            severe("Saving node database", x);
        }
    }

    /**
     * Called by saveDatabase, no need to explicitly call.
     */
    private void trimBackups() {
        final File nodes = config.getNodesFile();
        if (nodes == null) {
            return;
        }
        File dir = nodes.getAbsoluteFile().getParentFile();
        File[] backups = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".zip") && name.startsWith(nodes.getName());
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

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Initialization
    ///////////////////////////////////////////////////////////////////////////

}
