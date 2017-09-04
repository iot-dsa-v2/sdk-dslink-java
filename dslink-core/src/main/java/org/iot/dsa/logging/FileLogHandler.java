package org.iot.dsa.logging;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.iot.dsa.time.DSTime;
import org.iot.dsa.util.DSException;

/**
 * Logs records to a file.  When the file exceeds a certain childCount, it'll be zipped to a backup,
 * and excess backups will be deleted.
 *
 * @author Aaron Hansen
 */
public class FileLogHandler extends AsyncLogHandler {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private int backupThreshold = DSLogging.DEFAULT_BACKUP_THRESHOLD;
    private File file;
    private static Map<String, FileLogHandler> handlers =
            new HashMap<String, FileLogHandler>();
    private int maxBackups = DSLogging.DEFAULT_MAX_BACKUPS;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Will start by appending to an existing file.
     */
    private FileLogHandler(File file) {
        try {
            FileOutputStream fos = new FileOutputStream(file, true);
            BufferedOutputStream buf = new BufferedOutputStream(fos);
            setOut(new PrintStream(buf, true, "UTF-8"));
            this.file = file;
            start();
        } catch (Exception x) {
            DSException.throwRuntime(x);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Will return an existing handler for the given file, or create a new one.
     */
    public static synchronized FileLogHandler getHandler(File file) {
        String path = file.getAbsolutePath();
        FileLogHandler handler = handlers.get(path);
        if (handler == null) {
            handler = new FileLogHandler(file);
            handlers.put(path, handler);
        }
        return handler;
    }

    /**
     * Backup files for this logging, found in the same directory as the active logging.
     */
    public File[] getBackups() {
        File dir = file.getAbsoluteFile().getParentFile();
        File[] backups = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".zip") && name.startsWith(file.getName());
            }
        });
        if (backups == null) {
            return new File[0];
        }
        Arrays.sort(backups);
        return backups;
    }

    /**
     * The childCount after which a logging file will be backed up and cleared.
     */
    public int getBackupThreshold() {
        return backupThreshold;
    }

    /**
     * The number of backup files to retain.
     */
    public int getMaxBackups() {
        return maxBackups;
    }

    @Override
    public String getThreadName() {
        return "Log Handler " + file.getName();
    }

    @Override
    protected void houseKeeping() {
        if (file.length() > backupThreshold) {
            makeBackup();
            trimBackups();
        }
    }

    /**
     * Only public for testing, do not call.  Zips the current logging file, deletes the unzipped
     * version, then starts a new one.
     */
    public void makeBackup() {
        if (getMaxBackups() > 0) {
            ZipOutputStream zip = null;
            FileInputStream in = null;
            try {
                StringBuilder buf = new StringBuilder();
                buf.append(file.getName()).append('.');
                Calendar cal = DSTime.getCalendar(System.currentTimeMillis());
                DSTime.encodeForFiles(cal, buf);
                DSTime.recycle(cal);
                buf.append(".zip");
                File back = new File(file.getParent(), buf.toString());
                zip = new ZipOutputStream(new FileOutputStream(back));
                zip.putNextEntry(new ZipEntry(file.getName()));
                in = new FileInputStream(file);
                byte[] bytes = new byte[4096];
                int len = in.read(bytes);
                while (len > 0) {
                    zip.write(bytes, 0, len);
                    len = in.read(bytes);
                }
            } catch (Exception x) {
                Logger.getLogger("").log(Level.SEVERE, "Log backup error", x);
            }
            if (zip != null) {
                try {
                    zip.close();
                } catch (Exception x) {
                    Logger.getLogger("").log(Level.FINEST, "Log backup error", x);
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (Exception x) {
                    Logger.getLogger("").log(Level.FINEST, "Log backup error", x);
                }
            }
        }
        try {
            file.delete();
            setOut(new PrintStream(file, "UTF-8"));
        } catch (Exception e) {
            DSException.throwRuntime(e);
        }
    }

    /**
     * The file childCount threshold after which a logging file will be backed up and cleared.
     */
    public FileLogHandler setBackupThreshold(int arg) {
        backupThreshold = arg;
        return this;
    }

    /**
     * The default is 10.
     */
    public FileLogHandler setMaxBackups(int arg) {
        maxBackups = arg;
        return this;
    }

    /**
     * Only public for testing, do not call.  Deletes old zipped up logs.
     */
    public void trimBackups() {
        File[] backups = getBackups();
        if (backups.length <= maxBackups) {
            return;
        }
        for (int i = 0, len = backups.length - maxBackups; i < len; i++) {
            backups[i].delete();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Will attempt to close all the FileLogHandlers.
     */
    private static class ShutdownHook extends Thread {

        public void run() {
            ArrayList<FileLogHandler> list =
                    new ArrayList<FileLogHandler>(handlers.size());
            synchronized (handlers) {
                list.addAll(handlers.values());
            }
            for (FileLogHandler handler : list) {
                try {
                    handler.close();
                } catch (Exception ignore) {
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Initialization
    ///////////////////////////////////////////////////////////////////////////

    static {
        try {
            Runtime.getRuntime().addShutdownHook(new ShutdownHook());
        } catch (Throwable x) {
            Logger.getLogger("").log(Level.WARNING,
                                     "FileLogHandler: Unable to add shutdown hook", x);
        }
    }

} //class
