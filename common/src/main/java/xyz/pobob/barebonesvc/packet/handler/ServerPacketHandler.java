package xyz.pobob.barebonesvc.packet.handler;

@FunctionalInterface
public interface ServerPacketHandler {
    void handle(byte[] data);
}
