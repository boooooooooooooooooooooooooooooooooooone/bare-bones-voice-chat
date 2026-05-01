package xyz.pobob.barebonesvc.net;

import xyz.pobob.barebonesvc.util.Bytes;

/**
 * [MAGIC : 1][VERSION : 1][TYPE : 1][LENGTH : 2][PAYLOAD]
 */
public abstract class Packet {

    public static final byte MAGIC_BYTE = 0x65;
    public static final byte VERSION = 0x02;

    public Type type;

    public abstract byte[] serialize();

    public abstract void deserialize(byte[] data);

    public static boolean checkSignature(byte[] data) {
        return data[0] == Packet.MAGIC_BYTE && data[1] == Packet.VERSION;
    }

    public static short getPayloadLength(byte[] data) {
        return Bytes.getShort(data, 3);
    }

    public enum Type {
        CLIENT_HELLO((byte) 0x01),
        SERVER_HELLO((byte) 0x02),
        CLIENT_KEEP_ALIVE((byte) 0x03),
        SERVER_KEEP_ALIVE((byte) 0x04),
        SERVER_CLOSE((byte) 0x05),
        CLIENT_AUDIO((byte) 0x06),
        SERVER_AUDIO((byte) 0x07),
        CLIENT_UPDATE_PLAYER((byte) 0x08),
        SERVER_UPDATE_PLAYER((byte) 0x09);

        public final byte id;

        Type(byte id) {
            this.id = id;
        }

        public byte[] createHeader(int len) {
            return Bytes.join(
                    new byte[] {
                            MAGIC_BYTE,
                            VERSION,
                            this.id
                    },
                    Bytes.of((short) len)
            );
        }
    }
}