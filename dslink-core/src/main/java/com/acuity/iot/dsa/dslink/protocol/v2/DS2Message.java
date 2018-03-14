package com.acuity.iot.dsa.dslink.protocol.v2;

import java.util.Map;
import org.iot.dsa.logging.DSLogger;
import org.iot.dsa.node.DSBytes;

/**
 * Methods common to the reader and writer.
 *
 * @author Aaron Hansen
 */
abstract class DS2Message extends DSLogger implements MessageConstants {

    private StringBuilder logBuffer;

    protected StringBuilder debugMethod(int arg, StringBuilder buf) {
        if (buf == null) {
            buf = new StringBuilder();
        }
        switch (arg) {
            case MSG_SUBSCRIBE_REQ:
                buf.append("Sub req");
                break;
            case MSG_SUBSCRIBE_RES:
                buf.append("Sub res");
                break;
            case MSG_LIST_REQ:
                buf.append("List req");
                break;
            case MSG_LIST_RES:
                buf.append("List res");
                break;
            case MSG_INVOKE_REQ:
                buf.append("Invoke req");
                break;
            case MSG_INVOKE_RES:
                buf.append("Invoke res");
                break;
            case MSG_SET_REQ:
                buf.append("Set req");
                break;
            case MSG_SET_RES:
                buf.append("Set res");
                break;
            case MSG_OBSERVE_REQ:
                buf.append("Obs req");
                break;
            case MSG_CLOSE:
                buf.append("Close");
                break;
            case MSG_ACK:
                buf.append("Ack");
                break;
            case MSG_PING:
                buf.append("Ping");
                break;
            case MSG_HANDSHAKE_1:
                buf.append("Handshake 1");
                break;
            case MSG_HANDSHAKE_2:
                buf.append("Handshake 2");
                break;
            case MSG_HANDSHAKE_3:
                buf.append("Handshake 3");
                break;
            case MSG_HANDSHAKE_4:
                buf.append("Handshake 4");
                break;
            default:
                buf.append("0x");
                DSBytes.toHex((byte) arg, buf);
        }
        return buf;
    }

    private StringBuilder debugHeader(int arg, StringBuilder buf) {
        if (buf == null) {
            buf = new StringBuilder();
        }
        switch (arg) {
            case HDR_STATUS:
                buf.append("Status");
                break;
            case HDR_SEQ_ID:
                buf.append("Seq ID");
                break;
            case HDR_PAGE_ID:
                buf.append("Page ID");
                break;
            case HDR_ALIAS_COUNT:
                buf.append("Alias Ct");
                break;
            case HDR_PRIORITY:
                buf.append("Priority");
                break;
            case HDR_NO_STREAM:
                buf.append("No Stream");
                break;
            case HDR_QOS:
                buf.append("QOS");
                break;
            case HDR_QUEUE_SIZE:
                buf.append("Queue Size");
                break;
            case HDR_QUEUE_DURATION:
                buf.append("Queue Duration");
                break;
            case HDR_REFRESHED:
                buf.append("Refreshed");
                break;
            case HDR_PUB_PATH:
                buf.append("Pub Path");
                break;
            case HDR_SKIPPABLE:
                buf.append("Skippable");
                break;
            case HDR_MAX_PERMISSION:
                buf.append("Max Perm");
                break;
            case HDR_ATTRIBUTE_FIELD:
                buf.append("Attr Field");
                break;
            case HDR_PERMISSION_TOKEN:
                buf.append("Perm Token");
                break;
            case HDR_TARGET_PATH:
                buf.append("Target Path");
                break;
            case HDR_SOURCE_PATH:
                buf.append("Sourth Path");
                break;
            default:
                buf.append("0x");
                DSBytes.toHex((byte) arg, buf);
        }
        return buf;
    }

    protected void debugHeaders(Map<Integer, Object> headers, StringBuilder buf) {
        for (Map.Entry<Integer, Object> e : headers.entrySet()) {
            buf.append(", ");
            debugHeader(e.getKey(), buf);
            if (e.getValue() != NO_HEADER_VAL) {
                buf.append("=");
                buf.append(e.getValue());
            }
        }
    }

    protected abstract void getDebug(StringBuilder buf);

    private StringBuilder getLogBuffer() {
        if (logBuffer == null) {
            logBuffer = new StringBuilder();
        }
        return logBuffer;
    }

    protected void printDebug() {
        StringBuilder buf = getLogBuffer();
        getDebug(buf);
        debug(buf.toString());
        buf.setLength(0);
    }

}
