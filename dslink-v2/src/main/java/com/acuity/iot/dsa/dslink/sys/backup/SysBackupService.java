package com.acuity.iot.dsa.dslink.sys.backup;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.iot.dsa.DSRuntime;
import org.iot.dsa.DSRuntime.Timer;
import org.iot.dsa.dslink.ActionResults;
import org.iot.dsa.dslink.DSLink;
import org.iot.dsa.io.DSIWriter;
import org.iot.dsa.io.NodeEncoder;
import org.iot.dsa.io.json.Json;
import org.iot.dsa.node.DSBool;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSLong;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.action.DSAction;
import org.iot.dsa.node.action.DSIActionRequest;
import org.iot.dsa.time.DSDateTime;
import org.iot.dsa.time.Time;

/**
 * @author Daniel Shapiro
 * @author Aaron Hansen
 */
public class SysBackupService extends DSNode implements Runnable {

    static final String ENABLED = "Enabled";
    static final String INTERVAL = "Backup Interval";
    static final String LAST_DURATION = "Last Duration";
    static final String LAST_TIME = "Last Time";
    static final String MAXIMUM = "Max Backups";
    static final String SAVE = "Save";

    private DSInfo<?> enabled = getInfo(ENABLED);
    private DSInfo<?> interval = getInfo(INTERVAL);
    private DSInfo<?> lastDuration = getInfo(LAST_DURATION);
    private DSInfo<?> lastTime = getInfo(LAST_TIME);
    private DSLink link;
    private Object lock = new Object();
    private DSInfo<?> maximum = getInfo(MAXIMUM);
    private Timer nextSave;
    private boolean saving = false;
    private Timer saveDelay;

    public boolean isEnabled() {
        return enabled.getElement().toBoolean();
    }

    /**
     * Intended for use by DSLink subclasses, such as testing links.
     */
    public void setEnabled(boolean arg) {
        put(enabled, DSBool.valueOf(arg));
    }

    @Override
    public void onChildChanged(DSInfo<?> info) {
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
    public void onStopped() {
        if (nextSave != null) {
            nextSave.cancel();
            nextSave = null;
        }
        saveActual();
    }

    @Override
    public void run() {
        synchronized (lock) {
            saveActual();
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
        if (saveDelay != null) {
            saveDelay.cancel();
        }
        saveDelay = DSRuntime.runDelayed(this::saveActual, 5000);
    }

    @Override
    protected void declareDefaults() {
        declareDefault(SAVE, new SaveAction());
        declareDefault(ENABLED, DSBool.TRUE).setTransient(true);
        declareDefault(INTERVAL, DSLong.valueOf(60));
        declareDefault(LAST_TIME, DSDateTime.NULL).setReadOnly(true);
        declareDefault(LAST_DURATION, DSLong.NULL).setReadOnly(true);
        declareDefault(MAXIMUM, DSLong.valueOf(3));
    }

    @Override
    protected void onStable() {
        File nodes = getLink().getOptions().getNodesFile();
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
            link = getAncestor(DSLink.class);
        }
        return link;
    }

    /**
     * Serializes the configuration database.
     */
    private void saveActual() {
        if (saveDelay != null) {
            saveDelay.cancel();
            saveDelay = null;
        }
        if (!isEnabled()) {
            return;
        }
        synchronized (this) {
            if (saving) {
                return;
            }
            saving = true;
        }
        ZipOutputStream zos = null;
        InputStream in = null;
        try {
            File nodes = getLink().getOptions().getNodesFile();
            String name = nodes.getName();
            long time = System.currentTimeMillis();
            put(lastTime, DSDateTime.valueOf(time));
            // save tmp file
            File tmpNodes = saveTmpFile(nodes);
            // make backup
            if (nodes.exists()) {
                StringBuilder buf = new StringBuilder();
                Calendar cal = Time.getCalendar(System.currentTimeMillis());
                if (name.endsWith(".zip")) {
                    // just rename
                    String tmp = name.substring(0, name.lastIndexOf(".zip"));
                    buf.append(tmp).append('.');
                    Time.encodeForFiles(cal, buf);
                    buf.append(".zip");
                    File bakFile = new File(nodes.getParent(), buf.toString());
                    if (!nodes.renameTo(bakFile)) {
                        warn("Failed to rename %s to %s", nodes.getName(), bakFile.getName());
                    }
                } else {
                    // create zip
                    buf.append(name).append('.');
                    Time.encodeForFiles(cal, buf);
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
                    fos.getFD().sync();
                    if (!nodes.delete()) {
                        warn("Failed to delete after creating backup: %s", nodes.getName());
                    }
                }
                Time.recycle(cal);
            }
            // rename tmp
            if (!tmpNodes.renameTo(nodes)) {
                warn("Failed to rename %s to %s", tmpNodes.getName(), nodes.getName());
            }
            // cleanup
            trimBackups();
            time = System.currentTimeMillis() - time;
            put(lastDuration, DSLong.valueOf(time));
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
        synchronized (this) {
            saving = false;
        }
    }

    private File saveTmpFile(File nodes) throws Exception {
        String name = nodes.getName();
        long time = System.currentTimeMillis();
        put(lastTime, DSDateTime.valueOf(time));
        info("Saving node database " + nodes.getCanonicalPath());
        DSIWriter writer;
        File tmpNodes = new File(nodes.getParentFile(), name + ".tmp");
        if (tmpNodes.exists()) {
            if (!tmpNodes.delete()) {
                warn("Failed to delete existing tmp file: %s", tmpNodes.getName());
            }
        }
        FileOutputStream out = new FileOutputStream(tmpNodes);
        ZipOutputStream zout = null;
        if (name.endsWith(".zip")) {
            String tmp = name.substring(0, name.lastIndexOf(".zip"));
            zout = new ZipOutputStream(new BufferedOutputStream(out));
            zout.putNextEntry(new ZipEntry(tmp + ".json"));
            writer = Json.writer(zout);
        } else {
            writer = Json.writer(out);
        }
        NodeEncoder.encode(writer, getLink());
        writer.flush();
        if (zout != null) {
            zout.closeEntry();
        }
        writer.flush();
        out.getFD().sync();
        writer.close();
        if (tmpNodes.length() <= 0) {
            throw new IllegalStateException(name + " has no data!");
        }
        return tmpNodes;
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
        final File nodes = getLink().getOptions().getNodesFile();
        if (nodes == null) {
            return;
        }
        final String nodesName = nodes.getName();
        final boolean isZip = nodesName.endsWith(".zip");
        int idx = nodesName.lastIndexOf('.');
        final String nameBase = nodesName.substring(0, idx);
        File dir = nodes.getAbsoluteFile().getParentFile();
        File[] backups = dir.listFiles((dir1, name) -> {
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

    private static class SaveAction extends DSAction {

        @Override
        public ActionResults invoke(DSIActionRequest request) {
            ((SysBackupService) request.getTarget()).save();
            return null;
        }

    }

}
