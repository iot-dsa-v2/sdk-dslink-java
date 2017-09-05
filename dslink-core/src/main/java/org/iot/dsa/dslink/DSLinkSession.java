package org.iot.dsa.dslink;

/**
 * Represents a line of communication with a remote entity.
 *
 * @author Aaron Hansen
 */
public interface DSLinkSession {

    /**
     * The associate connection.
     */
    public DSLinkConnection getConnection();

    /**
     * Large outbound messages that can be broken into smaller ones should check this occasionally
     * when writing.  The idea is to prevent buffer overflows as well as not have a single message
     * hog communication.
     */
    public boolean shouldEndMessage();

}
