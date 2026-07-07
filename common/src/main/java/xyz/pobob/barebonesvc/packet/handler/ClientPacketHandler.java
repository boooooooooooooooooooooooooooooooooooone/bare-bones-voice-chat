package xyz.pobob.barebonesvc.packet.handler;

import java.net.SocketAddress;

@FunctionalInterface
public interface ClientPacketHandler {
    void handle(byte[] data, SocketAddress clientAddress);
}
