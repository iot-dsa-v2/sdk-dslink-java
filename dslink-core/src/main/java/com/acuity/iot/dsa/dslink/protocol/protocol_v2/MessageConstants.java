package com.acuity.iot.dsa.dslink.protocol.protocol_v2;

/**
 * Used to write a DSA 2.n message (header and body).  Call init(int,int) to start a new message,
 * can be reused for multiple messages. Not thread safe, the intent is messages will be constructed
 * and written serially.
 *
 * @author Aaron Hansen
 */
public interface MessageConstants {

    int MAX_HEADER = 1024 * 48;
    int MAX_BODY = 16320;

    Byte HDR_STATUS = 0x0;
    Byte HDR_SEQ_ID = 0x01;
    Byte HDR_PAGE_ID = 0x02;
    Byte HDR_ALIAS_COUNT = 0x08;
    Byte HDR_PRIORITY = 0x10;
    Byte HDR_NO_STREAM = 0x11;
    Byte HDR_QOS = 0x12;
    Byte HDR_QUEUE_SIZE = 0x14;
    Byte HDR_QUEUE_DURATION = 0x15;
    Byte HDR_REFRESHED = 0x20;
    Byte HDR_PUB_PATH = 0x21;
    Byte HDR_SKIPPABLE = 0x30;
    Byte HDR_MAX_PERMISSION = 0x32;
    Byte HDR_ATTRIBUTE_FIELD = 0x41;
    Byte HDR_PERMISSION_TOKEN = 0x60;
    Byte HDR_TARGET_PATH = (byte) (0x80 & 0xFF);
    Byte HDR_SOURCE_PATH = (byte) (0x81 & 0xFF);

    int MSG_SUBSCRIBE_REQ = 0x01;
    int MSG_SUBSCRIBE_RES = 0x81;
    int MSG_LIST_REQ = 0x02;
    int MSG_LIST_RES = 0x82;
    int MSG_INVOKE_REQ = 0x03;
    int MSG_INVOKE_RES = 0x83;
    int MSG_SET_REQ = 0x04;
    int MSG_SET_RES = 0x84;
    int MSG_OBSERVE_REQ = 0x0A;
    int MSG_OBSERVE_RES = 0x8A;
    int MSG_CLOSE = 0x0F;
    int MSG_ACK = 0xF8;
    int MSG_PING = 0xF9;

    Byte STS_OK = 0;
    Byte STS_INITIALIZING = 0x01;
    Byte STS_NOT_AVAILABLE = 0x0E;
    Byte STS_DROPPED = 0x10;
    Byte STS_CLOSED = 0x20;
    Byte STS_DISCONNECTED = 0x2E;
    Byte STS_PERMISSION_DENIED = 0x40;
    Byte STS_NOT_SUPPORTED = 0x41;
    Byte STS_INVALID_MESSAGE = 0x44;
    Byte STS_INVALID_PARAMETER = 0x45;
    Byte STS_BUSY = 0x48;
    Byte STS_ALIAS_LOOP = 0x61;
    Byte STS_INVALID_AUTH = (byte) (0xF9 & 0xFF);

}
