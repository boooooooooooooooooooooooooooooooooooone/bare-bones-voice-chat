package xyz.pobob.barebonesvc.net;

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

public class ClientMessageDispatcher {
    private final Map<Byte, ClientPacketHandler> handlers = new HashMap<>();

    public void register(PacketType packetType, ClientPacketHandler handler) {
        this.handlers.put(packetType.value, handler);
    }
    public void dispatch(byte[] data, SocketAddress clientAddress) {
        ClientPacketHandler handler = this.handlers.get(data[2]);

        if (handler != null) {
            handler.handle(data, clientAddress);
        }
    }
}