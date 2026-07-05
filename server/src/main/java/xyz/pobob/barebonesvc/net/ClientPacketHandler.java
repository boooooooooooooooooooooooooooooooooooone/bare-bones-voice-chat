package xyz.pobob.barebonesvc.net;

import java.net.SocketAddress;

@FunctionalInterface
public interface ClientPacketHandler {
    void handle(byte[] data, SocketAddress clientAddress);
}
