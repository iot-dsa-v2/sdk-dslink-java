package org.iot.dsa.dslink;

import com.acuity.iot.dsa.dslink.DS1LinkConnection;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.iot.dsa.DSRuntime;
import org.iot.dsa.dslink.responder.InboundInvokeRequest;
import org.iot.dsa.dslink.responder.InboundListRequest;
import org.iot.dsa.dslink.responder.InboundSetRequest;
import org.iot.dsa.dslink.responder.InboundSubscribeRequest;
import org.iot.dsa.dslink.responder.OutboundListResponse;
import org.iot.dsa.dslink.responder.SubscriptionCloseHandler;
import org.iot.dsa.io.NodeDecoder;
import org.iot.dsa.io.NodeEncoder;
import org.iot.dsa.io.json.JsonReader;
import org.iot.dsa.io.json.JsonWriter;
import org.iot.dsa.logging.DSLogging;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.action.ActionInvocation;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.DSAction;
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
public class DSLink extends DSNode implements DSResponder, Runnable {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    static final String NODES = "Nodes";
    static final String SAVE = "Save";

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSLinkConfig config;
    private DSLinkConnection connection;
    private String dsId;
    private DSKeys keys;
    private Logger logger;
    private String name;
    private DSInfo save = getInfo(SAVE);
    private DSRequester requester;

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
        declareDefault(SAVE, new DSAction()).setConfig(true);
    }

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

    public DSRequester getRequester() {
        return requester;
    }

    /**
     * Configures a link instance, whether it is new instance or deserialized.
     */
    protected void init(DSLinkConfig config) {
        this.config = config;
        DSLogging.setDefaultLevel(config.getLogLevel());
        name = config.getLinkName();
        keys = config.getKeys();
        logger = DSLogging.getLogger(name, config.getLogFile());
    }

    /**
     * Creates a link by first testing for an existing serialized database.
     *
     * @param config Configuration options
     */
    public static DSLink load(DSLinkConfig config) {
        Logger logger = DSLogging.getDefaultLogger();
        DSLink ret = null;
        File nodes = config.getNodesFile();
        if (nodes.exists()) {
            logger.info("Loading node database...");
            long time = System.currentTimeMillis();
            JsonReader reader = new JsonReader(nodes);
            DSNode node = NodeDecoder.decode(reader);
            reader.close();
            if (node instanceof DSLink) {
                ret = (DSLink) node;
                ret.init(config);
            } else { //TODO - remove after 10/1/17, converting early databases.
                ret = new DSLink();
                ret.init(config);
                ret.put(NODES, node);
                ret.save();
            }
            time = System.currentTimeMillis() - time;
            ret.info("Node database loaded: " + time + "ms");
        } else {
            logger.info("Creating new database...");
            ret = new DSLink();
            ret.init(config);
            String type = config.getConfig(DSLinkConfig.CFG_ROOT_TYPE, null);
            if (type == null) {
                throw new IllegalStateException("Config missing the root node type");
            }
            ret.config("Nodes type: " + type);
            try {
                DSNode node = (DSNode) Class.forName(type).newInstance();
                ret.put(NODES, node);
            } catch (Exception x) {
                DSException.throwRuntime(x);
            }
            ret.save();
        }
        DSNode tmp = new DSNode();
        tmp.add(ret.getLinkName(), ret);
        return ret;
    }

    /**
     * This is a convenience for DSLink.load(new DSLinkConfig(args)).run() and can be used as the
     * the main class for any link.
     */
    public static void main(String[] args) {
        DSLinkConfig cfg = new DSLinkConfig(args);
        DSLink link = DSLink.load(cfg);
        link.run();
    }

    /**
     * Handles the save action.
     */
    public ActionResult onInvoke(DSInfo actionInfo, ActionInvocation invocation) {
        if (actionInfo == save) {
            DSRuntime.run(new Runnable() {
                @Override
                public void run() {
                    save();
                }
            });
            return null;
        }
        return super.onInvoke(actionInfo, invocation);
    }

    /**
     * Responder implementation.  If one of the children in the path implements DSResponder, it will
     * be given responsibility for completing the request.
     */
    @Override
    public ActionResult onInvoke(InboundInvokeRequest request) {
        RequestPath path = new RequestPath(request.getPath(), this);
        if (path.isResponder()) {
            DSResponder responder = (DSResponder) path.getTarget();
            return responder.onInvoke(new InvokeWrapper(path, request));
        }
        DSInfo info = path.getInfo();
        if (!info.isAction()) {
            throw new DSRequestException("Not an action " + path.getPath());
        }
        DSAction action = info.getAction();
        return action.invoke(info, request);
    }

    /**
     * Responder implementation.  If one of the children in the path implements DSResponder, it will
     * be given responsibility for completing the request.
     */
    @Override
    public OutboundListResponse onList(InboundListRequest request) {
        RequestPath path = new RequestPath(request.getPath(), this);
        if (path.isResponder()) {
            DSResponder responder = (DSResponder) path.getTarget();
            return responder.onList(new ListWrapper(path.getPath(), request));
        }
        return new ListSubscriber(path, request);
    }

    /**
     * Responder implementation.  If one of the children in the path implements DSResponder, it will
     * be given responsibility for completing the request.
     */
    @Override
    public SubscriptionCloseHandler onSubscribe(InboundSubscribeRequest request) {
        RequestPath path = new RequestPath(request.getPath(), this);
        if (path.isResponder()) {
            DSResponder responder = (DSResponder) path.getTarget();
            return responder.onSubscribe(new SubscribeWrapper(path.getPath(), request));
        }
        return new ValueSubscriber(path, request);
    }


    /**
     * Responder implementation.  If one of the children in the path implements DSResponder, it will
     * be given responsibility for completing the request.
     */
    @Override
    public void onSet(InboundSetRequest request) {
        RequestPath path = new RequestPath(request.getPath(), this);
        if (path.isResponder()) {
            DSResponder responder = (DSResponder) path.getTarget();
            responder.onSet(new SetWrapper(path.getPath(), request));
        }
        DSNode parent = path.getParent();
        DSInfo info = path.getInfo();
        if (info.isReadOnly()) {
            throw new DSRequestException("Not writable: " + getPath());
        }
        //TODO verify incoming permission
        if (info.isNode()) {
            info.getNode().onSet(request.getValue());
            return;
        }
        DSIValue value = info.getValue();
        if (value == null) {
            if (info.getDefaultObject() instanceof DSIValue) {
                value = (DSIValue) info.getDefaultObject();
            }
        }
        if (value != null) {
            value = value.valueOf(request.getValue());
        } else {
            value = request.getValue();
        }
        parent.onSet(info, value);
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
                        save();
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
                save();
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
                connection = new DS1LinkConnection();
            }
            put("Connection", connection).setConfig(true).setTransient(true);
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
     * Serializes the configuration database.
     */
    public void save() {
        ZipOutputStream zos = null;
        InputStream in = null;
        try {
            File nodes = config.getNodesFile();
            if (nodes.exists()) {
                info("Backing up the node database");
                StringBuilder buf = new StringBuilder();
                buf.append(nodes.getName()).append('.');
                Calendar cal = DSTime.getCalendar(System.currentTimeMillis());
                DSTime.encodeForFiles(cal, buf);
                DSTime.recycle(cal);
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
            long time = System.currentTimeMillis();
            info("Saving node database");
            JsonWriter writer = new JsonWriter(nodes);
            NodeEncoder.encode(writer, this);
            writer.close();
            trimBackups();
            time = System.currentTimeMillis() - time;
            info("Node database saved: " + time + "ms");
        } catch (Exception x) {
            severe("Saving node database", x);
        }
        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException x) {
            severe("Closing input", x);
        }
        try {
            if (zos != null) {
                zos.close();
            }
        } catch (IOException x) {
            severe("Closing output", x);
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
