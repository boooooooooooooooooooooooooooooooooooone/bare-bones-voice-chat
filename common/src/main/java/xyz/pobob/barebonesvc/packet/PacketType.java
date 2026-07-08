package xyz.pobob.barebonesvc.packet;

public enum PacketType {

    CLIENT_HELLO(0x00, ClientHelloPacket.class),
    CLIENT_ACK(0x01, ClientAckPacket.class),
    CLIENT_KEEP_ALIVE(0x02, ClientKeepAlivePacket.class),
    CLIENT_AUDIO(0x03, ClientAudioPacket.class),
    CLIENT_UPDATE_PLAYER(0x04, ClientUpdatePlayerPacket.class),
    CLIENT_HASH(0x05, ClientHashPacket.class),

    SERVER_HELLO(0x00, ServerHelloPacket.class),
    SERVER_ACK(0x01, ServerAckPacket.class),
    SERVER_KEEP_ALIVE(0x02, ServerKeepAlivePacket.class),
    SERVER_AUDIO(0x03, ServerAudioPacket.class),
    SERVER_UPDATE_PLAYER(0x04, ServerUpdatePlayerPacket.class),
    SERVER_CLOSE(0x05, ServerClosePacket.class),
    SERVER_KICK(0x06, ServerKickPacket.class),
    SERVER_PLAYER_LATENCY(0x07, ServerPlayerLatencyPacket.class),
    SERVER_UPDATE_VOICE_DISTANCE(0x08, ServerUpdateVoiceDistancePacket.class),
    SERVER_AUTHENTICATED(0x09, ServerAuthenticatedPacket.class);

    public final byte id;
    public final Class<? extends Packet> packetClass;

    PacketType(int id, Class<? extends Packet> packetClass) {
        this.id = (byte) ((ReliablePacket.class.isAssignableFrom(packetClass)) ? (id | Packet.RELIABLE_MASK) : id);
        this.packetClass = packetClass;
    }
}