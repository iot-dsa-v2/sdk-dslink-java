package org.iot.dsa.logging;

import java.io.PrintStream;

/**
 * Async logging handler for writing to streams such as System.out.
 *
 * @author Aaron Hansen
 */
public class PrintStreamLogHandler extends AsyncLogHandler {

    private String name;

    public PrintStreamLogHandler(String name, PrintStream out) {
        this.name = name;
        setOut(out);
        start();
    }

    @Override
    public String getThreadName() {
        return name;
    }

}
