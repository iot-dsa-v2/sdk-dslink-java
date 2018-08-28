package com.acuity.iot.dsa.dslink.sys.backup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.iot.dsa.DSRuntime;
import org.iot.dsa.DSRuntime.Timer;
import org.iot.dsa.dslink.DSLink;
import org.iot.dsa.io.NodeEncoder;
import org.iot.dsa.io.json.JsonWriter;
import org.iot.dsa.node.DSBool;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSLong;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.action.ActionInvocation;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.DSAction;
import org.iot.dsa.time.DSTime;

/**
 * @author Daniel Shapiro
 * @author Aaron Hansen
 */
public class SysBackupService extends DSNode implements Runnable {

    static final String ENABLED = "Enabled";
    static final String INTERVAL = "Backup Interval";
    static final String MAXIMUM = "Max Backups";
    static final String SAVE = "Save";

    private DSInfo enabled = getInfo(ENABLED);
    private DSInfo interval = getInfo(INTERVAL);
    private DSLink link;
    private Object lock = new Object();
    private DSInfo maximum = getInfo(MAXIMUM);
    private Timer nextSave;
    private DSInfo save = getInfo(SAVE);

    public boolean isEnabled() {
        return enabled.getElement().toBoolean();
    }

    @Override
    public void onChildChanged(DSInfo info) {
        super.onChildChanged(info);
        if (info == interval) {
            DSIValue value = info.getValue();
            synchronized (lock) {
                if (nextSave != null) {
                    long newNextRun =
                            (value.toElement().toLong() * 60000) + System.currentTimeMillis();
                    long scheduledNextRun = nextSave.nextRun();
                    if (newNextRun < scheduledNextRun) {
                        nextSave.cancel();
                        nextSave = DSRuntime.runAt(this, newNextRun);
                    }
                }
            }
        }
    }

    @Override
    public ActionResult onInvoke(DSInfo action, ActionInvocation invocation) {
        if (action == save) {
            save();
        } else {
            super.onInvoke(action, invocation);
        }
        return null;
    }

    @Override
    public void onStopped() {
        if (nextSave != null) {
            nextSave.cancel();
            nextSave = null;
        }
        save();
    }

    @Override
    public void run() {
        synchronized (lock) {
            save();
            scheduleNextSave();
        }
    }

    /**
     * Serializes the configuration database.
     */
    public void save() {
        if (!isEnabled()) {
            return;
        }
        ZipOutputStream zos = null;
        InputStream in = null;
        try {
            File nodes = getLink().getConfig().getNodesFile();
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
            NodeEncoder.encode(writer, getLink());
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
     * Intended for use by DSLink subclasses, such as testing links.
     */
    public void setEnabled(boolean arg) {
        put(enabled, DSBool.valueOf(arg));
    }

    @Override
    protected void declareDefaults() {
        declareDefault(SAVE, DSAction.DEFAULT);
        declareDefault(ENABLED, DSBool.TRUE).setTransient(true);
        declareDefault(INTERVAL, DSLong.valueOf(60));
        declareDefault(MAXIMUM, DSLong.valueOf(3));
    }

    @Override
    protected void onStable() {
        File nodes = getLink().getConfig().getNodesFile();
        if (nodes.exists()) {
            synchronized (lock) {
                scheduleNextSave();
            }
        } else {
            DSRuntime.run(this);
        }
    }

    private DSLink getLink() {
        if (link == null) {
            link = (DSLink) getAncestor(DSLink.class);
        }
        return link;
    }

    private void scheduleNextSave() {
        long saveInterval = interval.getElement().toLong();
        saveInterval *= 60000;
        nextSave = DSRuntime.runDelayed(this, saveInterval);
    }

    /**
     * Called by save, no need to explicitly call.
     */
    private void trimBackups() {
        final File nodes = getLink().getConfig().getNodesFile();
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
        int maxBackups = maximum.getElement().toInt();
        if (backups.length <= maxBackups) {
            return;
        }
        for (int i = 0, len = backups.length - maxBackups; i < len; i++) {
            backups[i].delete();
        }
    }

}
