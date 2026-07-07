package xyz.pobob.barebonesvc.packet.registry;

import xyz.pobob.barebonesvc.packet.Packet;
import xyz.pobob.barebonesvc.packet.PacketType;
import xyz.pobob.barebonesvc.packet.handler.ClientPacketHandler;
import xyz.pobob.barebonesvc.packet.handler.ServerPacketHandler;

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

public final class PacketRegistry {

    private static final Map<Byte, ClientPacketHandler> CLIENT_PACKET_HANDLERS = new HashMap<>();
    private static final Map<Byte, ServerPacketHandler> SERVER_PACKET_HANDLERS = new HashMap<>();
    private static final Map<Class<? extends Packet>, Byte> PACKET_IDS = new HashMap<>();

    static {
        for (PacketType type : PacketType.values()) {
            PACKET_IDS.put(type.packetClass, type.id);
        }
    }

    public static void registerHandler(PacketType type, ClientPacketHandler handler) {
        CLIENT_PACKET_HANDLERS.put(type.id, handler);
    }

    public static void registerHandler(PacketType type, ServerPacketHandler handler) {
        SERVER_PACKET_HANDLERS.put(type.id, handler);
    }

    public static void dispatchServerPacket(byte[] data) {
        ServerPacketHandler handler = SERVER_PACKET_HANDLERS.get(data[Packet.TYPE_INDEX]);

        if (handler != null) {
            handler.handle(data);
        }
    }

    public static void dispatchClientPacket(byte[] data, SocketAddress clientAddress) {
        ClientPacketHandler handler = CLIENT_PACKET_HANDLERS.get(data[Packet.TYPE_INDEX]);

        if (handler != null) {
            handler.handle(data, clientAddress);
        }
    }

    public static byte getPacketId(Class<? extends Packet> packetClass) {
        return PACKET_IDS.get(packetClass);
    }
}