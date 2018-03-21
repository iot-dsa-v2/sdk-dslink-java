package org.iot.dsa.dslink;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.iot.dsa.logging.DSLogging;
import org.iot.dsa.logging.FileLogHandler;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Aaron Hansen
 */
public class LoggingTest {

    static Logger log;

    static {
        log = DSLogging.getLogger("test", new File("test.log"));
        log.setLevel(Level.ALL);
    }

    private void aMethod() {
        log.entering("LoggingTest", "aMethod");
        log.exiting("LoggingTest", "aMethod");
    }

    //@Test
    public void test1() throws Exception {
        Logger.getLogger("").info("My first log");
        log.info("My second log");
        //aMethod();
    }

    @Test
    public void test2() throws Exception {
        FileLogHandler handler = (FileLogHandler) log.getHandlers()[0];
        handler.flush();
        handler.setMaxBackups(2);
        handler.makeBackup();
        for (File f : handler.getBackups()) {
            f.delete();
        }
        for (int i = 0; i < 4; i++) {
            log.log(Level.SEVERE, "backup" + i, new Exception());
            Thread.sleep(1100);
            handler.flush();
            handler.makeBackup();
        }
        handler.trimBackups();
        File[] backups = handler.getBackups();
        Assert.assertTrue(backups.length == 2);
        for (File f : backups) {
            f.delete();
        }
    }


}
