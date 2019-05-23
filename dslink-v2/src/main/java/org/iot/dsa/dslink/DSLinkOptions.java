package org.iot.dsa.dslink;

import com.acuity.iot.dsa.dslink.protocol.DSKeys;
import com.acuity.iot.dsa.dslink.sys.logging.DSLevel;
import java.io.File;
import java.util.logging.Level;
import org.iot.dsa.io.json.Json;
import org.iot.dsa.node.DSMap;

/**
 * Configuration options for starting a link.  The base configuration is the file dslink.json.
 * Command line options can override the values in that file. More common options have getters and
 * setters as a convenience.
 *
 * @author Aaron Hansen
 */
public class DSLinkOptions {

    ///////////////////////////////////////////////////////////////////////////
    // Class Fields
    ///////////////////////////////////////////////////////////////////////////

    public static final String CFG_AUTH_TOKEN = "token";
    public static final String CFG_BROKER_URL = "broker";
    public static final String CFG_DSLINK_JSON = "dslink-json";
    public static final String CFG_KEY_FILE = "key";
    public static final String CFG_HOME = "home";
    //public static final String CFG_LOG_FILE = "log-file";
    public static final String CFG_LOG_LEVEL = "log";
    public static final String CFG_MAIN_NODE = "main-node";
    public static final String CFG_MSGPACK = "msgpack";
    public static final String CFG_NAME = "name";
    public static final String CFG_NODE_FILE = "nodes";

    public static final String CFG_CONNECTION_TYPE = "connectionType";
    public static final String CFG_READ_TIMEOUT = "readTimeout";
    public static final String CFG_STABLE_DELAY = "stableDelay";
    public static final String CFG_WS_TRANSPORT_FACTORY = "wsTransportFactory";

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    private String brokerUri;
    private String dslinkJson;
    private DSMap dslinkMap;
    private boolean help = false;
    private File home;
    private DSKeys keys;
    private String linkName;
    //private File logFile;
    private Level logLevel;
    private String mainType;
    private Boolean msgpack = null;
    private File nodesFile;
    private String token;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    /**
     * This will create an empty unvalidated getConfig.  This should not be used except for very
     * specific reasons such as testing.
     */
    public DSLinkOptions() {
    }

    /**
     * Can be use to change the working dir from the default process working dir.
     *
     * @param home Base directory used to resolve other files.
     */
    public DSLinkOptions(File home) {
        this.home = home;
    }

    /**
     * Constructor for simulating a command line invocation, the argument will be split on the space
     * character.
     */
    public DSLinkOptions(String args) {
        parse(args.split(" +"));
    }

    /**
     * Constructor for the arguments pass to a main method.
     */
    public DSLinkOptions(String[] args) {
        parse(args);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * If not set, will look for the getConfig in dslink.json.  This is a required getConfig so
     * it'll be validated as long as the public no-arg constructor is avoided.
     */
    public String getBrokerUri() {
        if (brokerUri == null) {
            brokerUri = getConfig(CFG_BROKER_URL, null);
        }
        return brokerUri;
    }

    /**
     * Looks for the value in dslink.json and if not found, returns the fallback.
     */
    public boolean getConfig(String name, boolean fallback) {
        DSMap map = getConfigMap(name, false);
        if (map == null) {
            return fallback;
        }
        return map.get("value", fallback);
    }

    /**
     * Looks for the value in dslink.json and if not found, returns the fallback.
     */
    public double getConfig(String name, double fallback) {
        DSMap map = getConfigMap(name, false);
        if (map == null) {
            return fallback;
        }
        return map.get("value", fallback);
    }

    /**
     * Looks for the value in dslink.json and if not found, returns the fallback.
     */
    public int getConfig(String name, int fallback) {
        DSMap map = getConfigMap(name, false);
        if (map == null) {
            return fallback;
        }
        return map.get("value", fallback);
    }

    /**
     * Looks for the value in dslink.json and if not found, returns the fallback.
     */
    public long getConfig(String name, long fallback) {
        DSMap map = getConfigMap(name, false);
        if (map == null) {
            return fallback;
        }
        return map.get("value", fallback);
    }

    /**
     * Looks for the value in dslink.json and if not found, returns the fallback.
     */
    public String getConfig(String name, String fallback) {
        DSMap map = getConfigMap(name, false);
        if (map == null) {
            return fallback;
        }
        return map.get("value", fallback);
    }

    /**
     * Uses the broker uri to determine the protocol version.
     */
    public String getDsaVersion() {
        return getDslinkMap().get("dsa-version", "1.0");
    }

    /**
     * Default is dslink.json.
     */
    public String getDslinkJson() {
        if (dslinkJson == null) {
            dslinkJson = "dslink.json";
        }
        return dslinkJson;
    }

    /**
     * If not set, this will attempt to open dslink.json.
     */
    public DSMap getDslinkMap() {
        if (dslinkMap == null) {
            File file = null;
            File home = this.home;
            if (home != null) {
                file = new File(home, getDslinkJson());
            } else {
                file = new File(getDslinkJson());
            }
            if (!file.exists()) {
                throw new IllegalStateException("Does not exist: " + file.getAbsolutePath());
            }
            dslinkMap = Json.read(file).toMap();
        }
        return dslinkMap;
    }

    /**
     * Help text for command line options.
     */
    public static String getHelpText() {
        StringBuilder buf = new StringBuilder();
        buf.append("Options can be specified as key=value or key value. The following are ")
           .append("all optional:\n\n")
           .append(" --broker       Broker URI for the connection handshake.\n")
           .append(" --dslink-json  Path to the dslink.json file, not related to the home dir.\n")
           .append(" --help|-h      Displays this message.\n")
           .append(" --home         Directory the link should use for file IO.  Default is process dir.\n")
           .append(" --key          Path to the stored keys file.\n")
           .append(" --log          Log level (finest,finer,fine,config,info,warning,severe).\n")
           .append(" --log-file     Path to the log file.\n")
           .append(" --msgpack      Whether or not to support msgpack (true by default).\n")
           .append(" --name         Alternate name for the link.\n")
           .append(" --nodes        Path to the configuration database file.\n")
           .append(" --token        Authentication token for the connection handshake.\n")
        ;
        return buf.toString();
    }

    /**
     * If not already set, will attempt to use CFG_HOME in dslink.json.  Whether or not the config
     * is present, home will be resolved against the directory of the process.
     */
    public File getHome() {
        if (home == null) {
            setHome(getConfig(CFG_HOME, null));
        }
        return home;
    }

    /**
     * If not set, will attempt to use the config in dslink.json (default is '.key') and resolve
     * against the home directory.  If the file does not exist, new keys will be created.
     */
    public DSKeys getKeys() {
        if (keys == null) {
            setKeys(new File(getHome(), getConfig(CFG_KEY_FILE, ".key")));
        }
        return keys;
    }

    public String getLinkName() {
        if (linkName == null) {
            linkName = getConfig(CFG_NAME, null);
        }
        if (linkName == null) {
            String text = "dslink-java-v2-";
            String s = getDslinkMap().getString(CFG_NAME);
            if (s.startsWith(text)) {
                s = s.substring(text.length());
            }
            linkName = s;
        }
        return linkName;
    }

    /**
     * Null by default.
     public File getLogFile() {
     if (logFile == null) {
     String path = getConfig(CFG_LOG_FILE, null);
     if (path != null) {
     logFile = new File(workingDir, path);
     }
     }
     return logFile;
     }
     */

    /**
     * Info by default.
     */
    public Level getLogLevel() {
        if (logLevel == null) {
            String level = getConfig(CFG_LOG_LEVEL, "info");
            logLevel = DSLevel.make(level).toLevel();
        }
        return logLevel;
    }

    /**
     * The type of the root node.
     */
    public String getMainType() {
        if (mainType == null) {
            mainType = getConfig(CFG_MAIN_NODE, null);
        }
        if (mainType == null) { //v1 sdk
            mainType = getConfig("handler_class", null);
        }
        if (mainType == null) {
            throw new IllegalStateException("Missing main node type in config.");
        }
        return mainType;
    }

    /**
     * Whether or not to support msgpack in the v1 protocol, true by default.
     */
    public boolean getMsgpack() {
        if (msgpack == null) {
            msgpack = getConfig(CFG_MSGPACK, true);
        }
        return msgpack;
    }

    /**
     * If not set, will attempt to use the config in dslink.json but fall back to 'nodes.zip'
     * if necessary.
     */
    public File getNodesFile() {
        if (nodesFile == null) {
            String path = getConfig(CFG_NODE_FILE, "nodes.zip");
            nodesFile = new File(getHome(), path);
        }
        return nodesFile;
    }

    /**
     * Authentication token for the broker, this can return null.
     */
    public String getToken() {
        if (token == null) {
            token = getConfig(CFG_AUTH_TOKEN, null);
        }
        return token;
    }

    public DSLinkOptions setBrokerUri(String arg) {
        brokerUri = arg;
        return this;
    }

    /**
     * Modifies the in-memory representation of dslink.json, but it will not be saved back to disk.
     */
    public DSLinkOptions setConfig(String name, boolean value) {
        DSMap map = getConfigMap(name, true);
        map.put("value", value);
        return this;
    }

    /**
     * Modifies the in-memory representation of dslink.json, but it will not be saved back to disk.
     */
    public DSLinkOptions setConfig(String name, double value) {
        DSMap map = getConfigMap(name, true);
        map.put("value", value);
        return this;
    }

    /**
     * Modifies the in-memory representation of dslink.json, but it will not be saved back to disk.
     */
    public DSLinkOptions setConfig(String name, int value) {
        DSMap map = getConfigMap(name, true);
        map.put("value", value);
        return this;
    }

    /**
     * Modifies the in-memory representation of dslink.json, but it will not be saved back to disk.
     */
    public DSLinkOptions setConfig(String name, long value) {
        DSMap map = getConfigMap(name, true);
        map.put("value", value);
        return this;
    }

    /**
     * Modifies the in-memory representation of dslink.json, but it will not be saved back to disk.
     */
    public DSLinkOptions setConfig(String name, String value) {
        DSMap map = getConfigMap(name, true);
        map.put("value", value);
        return this;
    }

    public DSLinkOptions setDslinkJson(String path) {
        dslinkJson = path;
        return this;
    }

    /**
     * Directly set the map without using file io.
     */
    public DSLinkOptions setDslinkMap(DSMap map) {
        dslinkMap = map;
        return this;
    }

    /**
     * Sets the home directory the link should use for file IO.  The path will be resolved
     * against the directory of the process "."
     */
    public DSLinkOptions setHome(String path) {
        home = new File(".");
        if ((path != null) && !path.isEmpty()) {
            home = new File(home, path);
        }
        home = home.getAbsoluteFile();
        return this;
    }

    /**
     * Directly set the keys without using file io.
     */
    public DSLinkOptions setKeys(DSKeys keys) {
        this.keys = keys;
        return this;
    }

    /**
     * Sets the link keys by file path.  Will throw a runtime exception if there are IO problems.
     */
    public DSLinkOptions setKeys(File file) {
        keys = new DSKeys(file);
        return this;
    }

    public DSLinkOptions setKeys(String path) {
        return setKeys(new File(getHome(), path));
    }

    public DSLinkOptions setLinkName(String arg) {
        linkName = arg;
        return this;
    }

    public DSLinkOptions setLogLevel(String level) {
        logLevel = DSLevel.make(level).toLevel();
        return this;
    }

    public DSLinkOptions setLogLevel(Level level) {
        logLevel = level;
        return this;
    }

    public DSLinkOptions setMainType(Class clazz) {
        return setMainType(clazz.getName());
    }

    public DSLinkOptions setMainType(String className) {
        mainType = className;
        return this;
    }

    public DSLinkOptions setMsgpack(boolean arg) {
        msgpack = arg;
        return this;
    }

    public DSLinkOptions setMsgpack(String str) {
        return setMsgpack("true".equalsIgnoreCase(str));
    }

    public DSLinkOptions setNodesFile(String path) {
        nodesFile = new File(getHome(), path);
        return this;
    }

    public DSLinkOptions setToken(String arg) {
        token = arg;
        return this;
    }

    /**
     * Whether or not -h or --help was provided.
     */
    public boolean wasHelpRequested() {
        return help;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Parses command line args to set the internal state of this object.
     *
     * @param args The argument passed to a main method.
     * @throws RuntimeException if there are any problems.
     */
    protected void parse(String[] args) {
        String key, value;
        int idx;
        for (int i = 0, len = args.length; i < len; i++) {
            key = args[i];
            value = null;
            idx = key.indexOf('=');
            if (idx > 0) {
                value = key.substring(idx + 1);
                key = key.substring(0, idx);
            } else if (!key.equals("-h") && !key.equals("--help")) {
                i++;
                if (i >= len) {
                    throw new IllegalArgumentException("Value not provided for " + key);
                }
                value = args[i];
            }
            processParsed(key, value);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Private Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Returns the map for the given config, creating if necessary.
     */
    private DSMap getConfigMap(String name, boolean create) {
        DSMap map = getDslinkMap();
        if (map == null) {
            if (!create) {
                return null;
            }
            map = new DSMap();
            setDslinkMap(map);
        }
        DSMap configs = map.getMap("configs");
        if (configs == null) {
            if (!create) {
                return null;
            }
            configs = new DSMap();
            map.put("configs", configs);
        }
        DSMap config = configs.getMap(name);
        if (config == null) {
            if (!create) {
                return null;
            }
            config = new DSMap();
            configs.put(name, config);
        }
        return config;
    }


    /**
     * Called by parseRequest for each key/value pair.
     *
     * @param value Can be null for simple switches like -h.
     */
    private void processParsed(String key, String value) {
        if (key.startsWith("--")) {
            if (key.equals("--broker")) {
                setBrokerUri(value);
            } else if (key.equals("--dslink-json")) {
                setDslinkJson(value);
            } else if (key.equals("--home")) {
                setHome(value);
            } else if (key.equals("--help")) {
                help = true;
            } else if (key.equals("--key")) {
                setKeys(value);
            } else if (key.equals("--log")) {
                setLogLevel(value);
                //} else if (key.equals("--log-file")) {
                //setLogFile(value);
            } else if (key.equals("--msgpack")) {
                setMsgpack(value);
            } else if (key.equals("--name")) {
                setLinkName(value);
            } else if (key.equals("--nodes")) {
                setNodesFile(value);
            } else if (key.equals("--token")) {
                setToken(value);
            } else {
                setConfig(key, value);
            }
        } else if (key.equals("-h")) {
            help = true;
        } else {
            throw new IllegalArgumentException("Unknown option: " + key);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Initialization
    ///////////////////////////////////////////////////////////////////////////

} //class
