package com.acuity.iot.dsa.dslink.transport;

import java.io.Reader;
import java.io.Writer;

/**
 * Transport that reads and writes text.
 *
 * @author Aaron Hansen
 */
public abstract class DSTextTransport extends DSTransport {

    public abstract Reader getReader();

    public abstract Writer getWriter();

}
