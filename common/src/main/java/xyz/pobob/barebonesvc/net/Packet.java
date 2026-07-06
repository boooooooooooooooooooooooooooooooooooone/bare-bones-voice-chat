package xyz.pobob.barebonesvc.net;

import xyz.pobob.barebonesvc.util.Bytes;

/**
 * [MAGIC : 1][VERSION : 1][TYPE : 1][LENGTH : 2][PAYLOAD]
 */
public interface Packet {

    byte MAGIC_BYTE = 0x65;
    byte VERSION = 0x06;
    byte RELIABLE_MASK = (byte) 0b10000000;

    int MAGIC_BYTE_INDEX = 0;
    int VERSION_INDEX = 1;
    int TYPE_INDEX = 2;
    int LENGTH_INDEX = 3;
    int PAYLOAD_INDEX = 5;

    byte[] serialize();

    void deserialize(byte[] data);

    static boolean checkSignature(byte[] data) {
        return data[MAGIC_BYTE_INDEX] == Packet.MAGIC_BYTE && data[VERSION_INDEX] == Packet.VERSION;
    }

    static boolean isReliable(byte[] data) {
        return (data[TYPE_INDEX] & RELIABLE_MASK) == RELIABLE_MASK;
    }

    static short getPayloadLength(byte[] data) {
        return Bytes.getShort(data, LENGTH_INDEX);
    }


    default byte[] createHeader(int len, PacketType type) {
        if (this instanceof ReliablePacket rp) {
            return Bytes.join(
                    new byte[] {
                            Packet.MAGIC_BYTE,
                            Packet.VERSION,
                            type.value
                    },
                    Bytes.of((short) len),
                    Bytes.of(rp.sequenceNumber)
            );
        } else {
            return Bytes.join(
                    new byte[] {
                            Packet.MAGIC_BYTE,
                            Packet.VERSION,
                            type.value
                    },
                    Bytes.of((short) len)
            );
        }
    }

    default int getPayloadIndex() {
        return (this instanceof ReliablePacket) ? ReliablePacket.PAYLOAD_INDEX : Packet.PAYLOAD_INDEX;
    }
}