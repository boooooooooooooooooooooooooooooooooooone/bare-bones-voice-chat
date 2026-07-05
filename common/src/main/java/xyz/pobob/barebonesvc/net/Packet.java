package xyz.pobob.barebonesvc.net;

import xyz.pobob.barebonesvc.util.Bytes;

/**
 * [MAGIC : 1][VERSION : 1][TYPE : 1][LENGTH : 2][PAYLOAD]
 */
public interface Packet {

    byte MAGIC_BYTE = 0x65;
    byte VERSION = 0x06;

    byte[] serialize();

    void deserialize(byte[] data);

    byte[] createHeader(int len);

    static boolean checkSignature(byte[] data) {
        return data[0] == Packet.MAGIC_BYTE && data[1] == Packet.VERSION;
    }

    static short getPayloadLength(byte[] data) {
        return Bytes.getShort(data, 3);
    }
}