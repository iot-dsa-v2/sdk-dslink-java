package org.iot.dsa.dslink;

import java.io.File;
import java.util.logging.Level;
import org.iot.dsa.io.json.JsonReader;
import org.iot.dsa.logging.DSILevels;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.security.DSKeys;

/**
 * Configuration options for starting a link.  The base configuration is the file dslink.json.
 * Command line options can override the values in that file. More common options have getters and
 * setters as a convenience.
 *
 * @author Aaron Hansen
 */
public class DSLinkConfig implements DSILevels {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    public static final String CFG_AUTH_TOKEN = "token";
    public static final String CFG_BROKER_URL = "broker";
    public static final String CFG_KEY_FILE = "key";
    public static final String CFG_LOG_LEVEL = "log";
    public static final String CFG_NODE_FILE = "nodes";

    public static final String CFG_CONNECTION_TYPE = "connectionType";
    public static final String CFG_READ_TIMEOUT = "readTimeout";
    public static final String CFG_SAVE_INTERVAL = "saveInterval";
    public static final String CFG_STABLE_DELAY = "stableDelay";
    public static final String CFG_WS_TRANSPORT_FACTORY = "wsTransportFactory";

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private String brokerUri;
    private String dsaVersion;
    private DSMap dslinkJson;
    private boolean help = false;
    private DSKeys keys;
    private String linkName;
    private Level logLevel;
    private File nodesFile;
    private String mainType;
    private String token;
    private File workingDir = new File(".");

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    /**
     * This will create an empty unvalidated getConfig.  This should not be used except for very
     * specific reasons such as testing.
     */
    public DSLinkConfig() {
    }

    /**
     * Can be use to change the working dir from the default process working dir.
     *
     * @param workingDir Base directory used to resolve other files.
     */
    public DSLinkConfig(File workingDir) {
        this.workingDir = workingDir;
        setDslinkJson(new File(workingDir, "dslink.json"));
    }

    /**
     * Constructor for simulating a command line invocation, the argument will be split on the space
     * character.
     */
    public DSLinkConfig(String args) {
        setDslinkJson(new File("dslink.json"));
        parse(args.split(" +"));
    }

    /**
     * Constructor for the arguments pass to a main method.
     */
    public DSLinkConfig(String[] args) {
        setDslinkJson(new File("dslink.json"));
        parse(args);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * If not set, will look for the getConfig in dslink.json.  This is a required getConfig so
     * it'll be validated as long as the public no-arg constructor is avoided.
     */
    public String getBrokerUri() {
        if (brokerUri == null) {
            brokerUri = getConfig(CFG_BROKER_URL, "http://localhost:8080/conn");
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
     * Returns the map for the given config, creating if necessary.
     */
    private DSMap getConfigMap(String name, boolean create) {
        DSMap map = getDslinkJson();
        if (map == null) {
            if (!create) {
                return null;
            }
            map = new DSMap();
            setDslinkJson(map);
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

    public String getDsaVersion() {
        String brokerUri = getBrokerUri();
        if (brokerUri != null) {
            if (brokerUri.startsWith("http")) {
                if (brokerUri.indexOf("/conn") > 0) {
                    return "1.0";
                }
            }
        }
        if (dsaVersion == null) {
            dsaVersion = getDslinkJson().get("dsa-version", "2.0");
        }
        return dsaVersion;
    }

    /**
     * If not set, this will attempt to open dslink.json in the working the process directory.
     */
    public DSMap getDslinkJson() {
        if (dslinkJson == null) {
            setDslinkJson(new File("dslink.json"));
        }
        return dslinkJson;
    }

    /**
     * Help text for command line options.
     */
    public static String getHelpText() {
        StringBuilder buf = new StringBuilder();
        buf.append("Options can be specified as key=value or key value. The following are ")
           .append("all optional:\n\n")
           .append(" --broker|-b  Required, broker URI for the connection handshake.\n")
           .append(" --token|-t   Authentication token for the connection handshake.\n")
           .append(" --nodes|-n   Path to the configuration node database.\n")
           .append(" --key|-k     Path to the stored keys.\n")
           .append(" --log|-l     Log level (finest,finer,fine,config,info,warning,severe).\n")
           .append(" --dslink-json|-d  Location of the dslink.json file.\n")
           .append(" --name       Name of the link implementation, for example ")
           .append("dslink-java-modbus.\n")
           .append(" --help|-h    Displays this message.\n");
        return buf.toString();
    }

    /**
     * If not set, will attempt to use the getConfig in dslink.json and fall back to '.key' in the
     * process directory if necessary.  If the file does not exist, new keys will be created.
     */
    public DSKeys getKeys() {
        if (keys == null) {
            setKeys(new File(getConfig(CFG_KEY_FILE, ".key")));
        }
        return keys;
    }

    public String getLinkName() {
        if (linkName == null) {
            linkName = getDslinkJson().getString("name");
        }
        return linkName;
    }

    /**
     * If not set, will attempt to use the getConfig in dslink.json but fall back to 'info' if
     * necessary.
     */
    public Level getLogLevel() {
        if (logLevel == null) {
            setLogLevel(getConfig(CFG_LOG_LEVEL, "info"));
        }
        return logLevel;
    }

    /**
     * If not set, will attempt to use the getConfig in dslink.json but fall back to 'nodes.zip' in
     * the process directory if necessary.
     */
    public File getNodesFile() {
        if (nodesFile == null) {
            setNodesFile(new File(getConfig(CFG_NODE_FILE, "nodes.zip")));
        }
        return nodesFile;
    }

    /**
     * The type of the root node.
     */
    public String getMainType() {
        if (mainType == null) { //legacy
            mainType = getConfig("handler_class", null);
        }
        if (mainType == null) { //legacy
            mainType = getConfig("mainType", null);
        }
        if (mainType == null) {
            throw new IllegalStateException("Missing main node type in config.");
        }
        return mainType;
    }

    /**
     * Authentication token for the broker, this can return null.
     */
    public String getToken() {
        if (token == null) {
            setToken(getConfig(CFG_AUTH_TOKEN, null));
        }
        return token;
    }

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
                setDslinkJson(new File(value));
            } else if (key.equals("--help")) {
                help = true;
            } else if (key.equals("--log")) {
                setLogLevel(value);
            } else if (key.equals("--logging")) {
                setLogLevel(value);
            } else if (key.equals("--key")) {
                setKeys(new File(value));
            } else if (key.equals("--name")) {
                setLinkName(value);
            } else if (key.equals("--nodes")) {
                setNodesFile(new File(value));
            } else if (key.equals("--token")) {
                setToken(value);
            } else {
                setConfig(key, value);
            }
        } else {
            if (key.equals("-b")) {
                setBrokerUri(value);
            } else if (key.equals("-d")) {
                setDslinkJson(new File(value));
            } else if (key.equals("-h")) {
                help = true;
            } else if (key.equals("-l")) {
                setLogLevel(value);
            } else if (key.equals("-k")) {
                setKeys(new File(value));
            } else if (key.equals("-n")) {
                setNodesFile(new File(value));
            } else if (key.equals("-t")) {
                setToken(value);
            } else {
                throw new IllegalArgumentException("Unknown option: " + key);
            }
        }
    }

    public DSLinkConfig setBrokerUri(String arg) {
        brokerUri = arg;
        return this;
    }

    /**
     * Modifies the in-memory representation of dslink.json, but it will not be saved back to disk.
     */
    public DSLinkConfig setConfig(String name, boolean value) {
        DSMap map = getConfigMap(name, true);
        map.put("value", value);
        return this;
    }

    /**
     * Modifies the in-memory representation of dslink.json, but it will not be saved back to disk.
     */
    public DSLinkConfig setConfig(String name, double value) {
        DSMap map = getConfigMap(name, true);
        map.put("value", value);
        return this;
    }

    /**
     * Modifies the in-memory representation of dslink.json, but it will not be saved back to disk.
     */
    public DSLinkConfig setConfig(String name, int value) {
        DSMap map = getConfigMap(name, true);
        map.put("value", value);
        return this;
    }

    /**
     * Modifies the in-memory representation of dslink.json, but it will not be saved back to disk.
     */
    public DSLinkConfig setConfig(String name, long value) {
        DSMap map = getConfigMap(name, true);
        map.put("value", value);
        return this;
    }

    /**
     * Modifies the in-memory representation of dslink.json, but it will not be saved back to disk.
     */
    public DSLinkConfig setConfig(String name, String value) {
        DSMap map = getConfigMap(name, true);
        map.put("value", value);
        return this;
    }

    /**
     * Directly set the map without using file io.
     */
    public DSLinkConfig setDslinkJson(DSMap map) {
        dslinkJson = map;
        return this;
    }

    /**
     * Sets the link map by file path.  Will throw a runtime exception if there are IO problems.
     */
    public DSLinkConfig setDslinkJson(File file) {
        if (!file.exists()) {
            throw new IllegalStateException("Does not exist: " + file.getAbsolutePath());
        }
        JsonReader in = new JsonReader(file);
        this.dslinkJson = in.getMap();
        in.close();
        return this;
    }

    /**
     * Directly set the keys without using file io.
     */
    public DSLinkConfig setKeys(DSKeys keys) {
        this.keys = keys;
        return this;
    }

    /**
     * Sets the link keys by file path.  Will throw a runtime exception if there are IO problems.
     */
    public DSLinkConfig setKeys(File file) {
        keys = new DSKeys(file);
        return this;
    }

    /**
     * Overrides dslink.json.
     */
    public DSLinkConfig setLinkName(String arg) {
        linkName = arg;
        return this;
    }

    /**
     * Should be one of the following (case insensitive): all, finest, finer, fine, config, info,
     * warning, severe, off. <p> Overrides dslink.json.
     */
    public DSLinkConfig setLogLevel(String level) {
        level = level.toUpperCase();
        if (level.equals("TRACE")) {
            logLevel = trace;
        } else if (level.equals("DEBUG")) {
            logLevel = debug;
        } else if (level.equals("FINE")) {
            logLevel = fine;
        } else if (level.equals("CONFIG")) {
            logLevel = fine;
        } else if (level.equals("WARN")) {
            logLevel = warn;
        } else if (level.equals("INFO")) {
            logLevel = info;
        } else if (level.equals("ERROR")) {
            logLevel = error;
        } else if (level.equals("ADMIN")) {
            logLevel = admin;
        } else if (level.equals("CRITICAL")) {
            logLevel = fatal;
        } else if (level.equals("FATAL")) {
            logLevel = fatal;
        } else if (level.equals("NONE")) {
            logLevel = off;
        } else {
            logLevel = Level.parse(level);
        }
        return setConfig(CFG_LOG_LEVEL, level);
    }

    /**
     * Overrides dslink.json.
     */
    public DSLinkConfig setLogLevel(Level level) {
        logLevel = level;
        return setConfig(CFG_LOG_LEVEL, level.toString());
    }

    /**
     * Overrides dslink.json.
     */
    public DSLinkConfig setNodesFile(File file) {
        nodesFile = file;
        return setConfig(CFG_NODE_FILE, file.getAbsolutePath());
    }

    /**
     * The type of the root node.
     */
    public DSLinkConfig setMainType(Class clazz) {
        mainType = clazz.getName();
        return this;
    }

    /**
     * Overrides dslink.json.
     */
    public DSLinkConfig setToken(String arg) {
        token = arg;
        return setConfig(CFG_AUTH_TOKEN, arg);
    }

    /**
     * Whether or not -h or --help was provided.
     */
    public boolean wasHelpRequested() {
        return help;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Initialization
    ///////////////////////////////////////////////////////////////////////////

} //class