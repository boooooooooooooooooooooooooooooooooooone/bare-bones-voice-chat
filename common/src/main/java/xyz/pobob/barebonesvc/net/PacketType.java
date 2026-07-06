package xyz.pobob.barebonesvc.net;

public enum PacketType {

    CLIENT_HELLO((byte) 0x00),
    CLIENT_KEEP_ALIVE((byte) 0x01),
    CLIENT_AUDIO((byte) 0x02),
    CLIENT_ACK((byte) 0x03),

    CLIENT_UPDATE_PLAYER((byte) 0x80),


    SERVER_KEEP_ALIVE((byte) 0x00),
    SERVER_AUDIO((byte) 0x01),
    SERVER_ACK((byte) 0x02),
    SERVER_CLOSE((byte) 0x03),
    SERVER_KICK((byte) 0x04),
    SERVER_PLAYER_LATENCY((byte) 0x05),

    SERVER_HELLO((byte) 0x80),
    SERVER_UPDATE_PLAYER((byte) 0x81),
    SERVER_UPDATE_VOICE_DISTANCE((byte) 0x82);

    public final byte value;

    PacketType(byte value) {
        this.value = value;
    }
}
