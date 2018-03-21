package com.acuity.iot.dsa.dslink.protocol.v2;

/**
 * Used to write a DSA 2.n message (header and body).  Call init(int,int) to start a new message,
 * can be reused for multiple messages. Not thread safe, the intent is messages will be constructed
 * and written serially.
 *
 * @author Aaron Hansen
 */
public interface MessageConstants {

    int MAX_BODY = 1024 * 48;
    int MAX_HEADER = 16320;
    Object NO_HEADER_VAL = new Object();

    int HDR_STATUS = 0x0;
    int HDR_SEQ_ID = 0x01;
    int HDR_PAGE_ID = 0x02;
    int HDR_AUDIT = 0x04;
    int HDR_ERROR_DETAIL = 0x05;
    int HDR_ALIAS_COUNT = 0x08;
    int HDR_PRIORITY = 0x10;
    int HDR_NO_STREAM = 0x11;
    int HDR_QOS = 0x12;
    int HDR_QUEUE_SIZE = 0x14;
    int HDR_QUEUE_DURATION = 0x15;
    int HDR_REFRESHED = 0x20;
    int HDR_PUB_PATH = 0x21;
    int HDR_SKIPPABLE = 0x30;
    int HDR_MAX_PERMISSION = 0x32;
    int HDR_ATTRIBUTE_FIELD = 0x41;
    int HDR_PERMISSION_TOKEN = 0x60;
    int HDR_TARGET_PATH = 0x80;
    int HDR_SOURCE_PATH = 0x81;

    int MSG_SUBSCRIBE_REQ = 0x01;
    int MSG_SUBSCRIBE_RES = 0x81;
    int MSG_LIST_REQ = 0x02;
    int MSG_LIST_RES = 0x82;
    int MSG_INVOKE_REQ = 0x03;
    int MSG_INVOKE_RES = 0x83;
    int MSG_SET_REQ = 0x04;
    int MSG_SET_RES = 0x84;
    int MSG_OBSERVE_REQ = 0x0A;
    int MSG_CLOSE = 0x0F;
    int MSG_ACK = 0xF8;
    int MSG_PING = 0xF9;
    int MSG_HANDSHAKE_1 = 0xF0;
    int MSG_HANDSHAKE_2 = 0xF1;
    int MSG_HANDSHAKE_3 = 0xF2;
    int MSG_HANDSHAKE_4 = 0xF3;

    byte STS_OK = 0;
    byte STS_INITIALIZING = 0x01;
    byte STS_NOT_AVAILABLE = 0x0E;
    byte STS_DROPPED = 0x10;
    byte STS_CLOSED = 0x20;
    byte STS_DISCONNECTED = 0x2E;
    byte STS_PERMISSION_DENIED = 0x40;
    byte STS_NOT_SUPPORTED = 0x41;
    byte STS_INVALID_MESSAGE = 0x44;
    byte STS_INVALID_PARAMETER = 0x45;
    byte STS_BUSY = 0x48;
    byte STS_INTERNAL_ERR = 0x50;
    byte STS_ALIAS_LOOP = 0x61;
    byte STS_INVALID_AUTH = (byte) 0xF9;

}
