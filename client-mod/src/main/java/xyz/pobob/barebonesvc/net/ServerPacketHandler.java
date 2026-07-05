package xyz.pobob.barebonesvc.net;

@FunctionalInterface
public interface ServerPacketHandler {
    void handle(byte[] data);
}
