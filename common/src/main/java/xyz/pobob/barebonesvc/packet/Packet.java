package xyz.pobob.barebonesvc.packet;

import xyz.pobob.barebonesvc.packet.registry.PacketRegistry;
import xyz.pobob.barebonesvc.util.Bytes;

public interface Packet {

    byte MAGIC_BYTE = 0x65;
    byte VERSION = 0x07;
    byte RELIABLE_MASK = (byte) 0b10000000;

    int MAGIC_BYTE_INDEX = 0;
    int VERSION_INDEX = 1;
    int TYPE_INDEX = 2;
    int LENGTH_INDEX = 3;
    int PAYLOAD_INDEX = 5;

    byte[] serialize();

    void deserialize(byte[] data);

    default byte[] createHeader(int len) {
        byte id = PacketRegistry.getPacketId(this.getClass());

        if (this instanceof ReliablePacket rp) {
            return Bytes.join(
                    new byte[] {
                            Packet.MAGIC_BYTE,
                            Packet.VERSION,
                            id
                    },
                    Bytes.of((short) len),
                    Bytes.of(rp.sequenceNumber)
            );
        } else {
            return Bytes.join(
                    new byte[] {
                            Packet.MAGIC_BYTE,
                            Packet.VERSION,
                            id
                    },
                    Bytes.of((short) len)
            );
        }
    }

    static boolean checkSignature(byte[] data) {
        return data[MAGIC_BYTE_INDEX] == Packet.MAGIC_BYTE && data[VERSION_INDEX] == Packet.VERSION;
    }

    static boolean isReliable(byte[] data) {
        return (data[TYPE_INDEX] & RELIABLE_MASK) == RELIABLE_MASK;
    }

    static short getPayloadLength(byte[] data) {
        return Bytes.getShort(data, LENGTH_INDEX);
    }

    default int getPayloadStart() {
        return (this instanceof ReliablePacket) ? ReliablePacket.PAYLOAD_INDEX : Packet.PAYLOAD_INDEX;
    }
}