package xyz.pobob.barebonesvc.net;

import xyz.pobob.barebonesvc.voiceserver.VoiceServer;

import java.net.SocketAddress;


public abstract class ClientPacketHandler {

    protected final VoiceServer server;

    public ClientPacketHandler(VoiceServer server) {
        this.server = server;
    }

    abstract void handle(byte[] data, SocketAddress clientAddress);
}
