package xyz.pobob.barebonesvc.net;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectArrayMap;

import java.util.Map;

public class ServerMessageDispatcher {
    private final Map<Byte, ServerPacketHandler> handlers = new Byte2ObjectArrayMap<>();

    public void register(PacketType packetType, ServerPacketHandler handler) {
        this.handlers.put(packetType.value, handler);
    }

    public void dispatch(byte[] data) {
        ServerPacketHandler handler = this.handlers.get(data[2]);

        if (handler != null) {
            handler.handle(data);
        }
    }
}
