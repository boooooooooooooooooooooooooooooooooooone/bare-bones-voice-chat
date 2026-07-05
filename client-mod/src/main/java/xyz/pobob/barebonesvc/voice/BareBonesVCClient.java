package xyz.pobob.barebonesvc.voice;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.debug.CooldownTimer;
import de.maxhenkel.voicechat.voice.client.AudioChannel;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.client.ClientVoicechat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import xyz.pobob.barebonesvc.BareBonesVC;
import xyz.pobob.barebonesvc.mixin.ClientVoicechatAccessor;
import xyz.pobob.barebonesvc.net.*;
import xyz.pobob.barebonesvc.voice.thread.ClientHandshakeThread;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BareBonesVCClient {

    private final ClientHelloPacket clientHelloPacket = new ClientHelloPacket();
    private final ClientUpdatePlayerPacket clientUpdatePlayerPacket = new ClientUpdatePlayerPacket();

    private final byte[] recvBuf = new byte[4096];
    private final DatagramPacket recvPacket = new DatagramPacket(recvBuf, recvBuf.length);
    private final byte[] sendBuf = new byte[4096];
    private final DatagramPacket sendPacket = new DatagramPacket(sendBuf, 0);

    private final ExecutorService pool = Executors.newSingleThreadExecutor();
    public final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private DatagramSocket socket;
    private Thread clientHandshakeThread;
    private Thread networkReceiveThread;

    public ClientVoicechat client;
    public volatile SessionConfig config;
    public long lastKeepAlive = 0;

    public static final BareBonesVCClient INSTANCE = new BareBonesVCClient();

    private volatile boolean running = false;
    private volatile boolean resolved = false;

    public void start(String host, int port) {
        this.client = null;
        this.config = null;

        InetSocketAddress address = new InetSocketAddress(host, port);
        if (address.isUnresolved()) {
            BareBonesVCClient.invalidAddress();
            this.resolved = false;
            return;
        } else {
            this.recvPacket.setSocketAddress(address);
            this.sendPacket.setSocketAddress(address);
            this.resolved = true;
        }

        try {
            this.socket = new DatagramSocket();
        } catch (SocketException e) {
            BareBonesVC.LOGGER.error("An error occurred while starting voice client", e);
            return;
        }
        this.running = true;

        final ServerMessageDispatcher serverMessageDispatcher = new ServerMessageDispatcher();
        serverMessageDispatcher.register(PacketType.SERVER_HELLO, new ServerHelloHandler());
        serverMessageDispatcher.register(PacketType.SERVER_KEEP_ALIVE, new ServerKeepAliveHandler());
        serverMessageDispatcher.register(PacketType.SERVER_AUDIO, new ServerAudioHandler());
        serverMessageDispatcher.register(PacketType.SERVER_UPDATE_PLAYER, new ServerUpdatePlayerHandler());
        serverMessageDispatcher.register(PacketType.SERVER_CLOSE, new ServerCloseHandler());
        serverMessageDispatcher.register(PacketType.SERVER_KICK, new ServerKickHandler());
        serverMessageDispatcher.register(PacketType.SERVER_UPDATE_VOICE_DISTANCE, new ServerUpdateVoiceDistanceHandler());
        serverMessageDispatcher.register(PacketType.SERVER_PLAYER_LATENCY, new ServerPlayerLatencyHandler());

        this.clientHelloPacket.create(
                MinecraftClient.getInstance().getGameProfile().name(),
                MinecraftClient.getInstance().getGameProfile().id(),
                VoicechatClient.CLIENT_CONFIG.disabled.get()
        );

        if (this.clientHandshakeThread != null) this.clientHandshakeThread.interrupt();
        if (this.networkReceiveThread != null) this.networkReceiveThread.interrupt();

        this.clientHandshakeThread = new ClientHandshakeThread(this.clientHelloPacket.serialize());
        this.networkReceiveThread = new Thread(() -> {
            while (this.isRunning()) {
                final byte[] data;
                try {
                    data = this.receive();
                    if (data.length < 5) continue;
                } catch (IOException e) {
                    continue;
                }

                pool.submit(() -> {
                    if (Packet.checkSignature(data)) {
                        serverMessageDispatcher.dispatch(data);
                    }
                });
            }

            this.stopNow();
        });
        this.clientHandshakeThread.start();
        this.networkReceiveThread.setName("BareBonesVCNetworkThread");
        this.networkReceiveThread.start();

        BareBonesVC.LOGGER.info("Started connecting to voice server {}", BareBonesVCClient.INSTANCE.getReadableAddress());
    }

    public void startVoiceChat() {

        if (VoicechatClient.CLIENT_CONFIG.muteOnJoin.get()) {
            ClientManager.getPlayerStateManager().setMuted(true);
        }

        this.client = new ClientVoicechat();
        if (this.client.getMicThread() != null) {
            this.client.getMicThread().close();
        }
        ((ClientVoicechatAccessor) this.client).invokeStartMicThread(null);
        BareBonesVC.LOGGER.info("Starting microphone thread");

    }

    public synchronized boolean send(byte[] data) {
        if (!this.isRunning()) return false;

        this.sendPacket.setLength(data.length);
        System.arraycopy(data, 0, this.sendPacket.getData(), 0, data.length);

        try {
            this.socket.send(this.sendPacket);
            return true;
        } catch (IOException e) {
            BareBonesVC.LOGGER.error("An error occurred while sending Datagram packet", e);
            return false;
        }
    }

    public byte[] receive() throws IOException {
        if (!this.isRunning()) return null;

        this.socket.receive(this.recvPacket);

        if (this.recvPacket.getLength() >= this.recvPacket.getData().length) {
            CooldownTimer.run("udp_packet_too_large", () ->
                    BareBonesVC.LOGGER.warn("Packet from {} is too large", this.getReadableAddress()));
            throw new IOException(String.format("Packet from %s is too large", this.getReadableAddress()));
        }

        byte[] data = new byte[this.recvPacket.getLength()];
        System.arraycopy(this.recvPacket.getData(), this.recvPacket.getOffset(), data, 0, this.recvPacket.getLength());
        return data;
    }

    public void declareOwnState(boolean disabled) {
        this.clientUpdatePlayerPacket.create(disabled, false);
        byte[] serialized = this.clientUpdatePlayerPacket.serialize();
        this.send(serialized);
        this.scheduler.schedule(() -> this.send(serialized), 1000, TimeUnit.MILLISECONDS);
        this.scheduler.schedule(() -> this.send(serialized), 2000, TimeUnit.MILLISECONDS);
    }

    public void disconnect() {
        BareBonesVC.LOGGER.info("Disconnected from {}", this.getReadableAddress());

        if (this.isRunning()) {
            this.clientUpdatePlayerPacket.create(ClientManager.getPlayerStateManager().isDisabled(), true);
            this.send(this.clientUpdatePlayerPacket.serialize());
        }
        this.clearStates();

        if (this.client != null) {
            this.client.closeMicThread();
            this.client.close();
            this.client = null;
        }

        this.stopNow();
    }

    public void stopNow() {
        if (this.isRunning()) {
            this.running = false;
        }

        if (this.socket != null) {
            this.resolved = false;
            this.socket.close();
            this.socket = null;
        }
    }

    public boolean isRunning() {
        return this.running && this.resolved;
    }

    public boolean isConnected() {
        return this.running && System.currentTimeMillis() - this.lastKeepAlive < 30000;
    }

    public synchronized Map<UUID, AudioChannel> getAudioChannels() {
        return BareBonesVCClient.INSTANCE.client.getAudioChannels();
    }

    private synchronized void clearStates() {
        ClientManager.getPlayerStateManager().clearStates();
    }

    public static void invalidAddress() {
        if (MinecraftClient.getInstance().player != null) {
            MinecraftClient.getInstance().player.sendMessage(Text.of("Failed to resolve address"), true);
        }
    }

    public String getReadableAddress() {
        return ((this.sendPacket.getAddress() == null) ? "null" : this.sendPacket.getAddress().getHostAddress()) + ":" + this.sendPacket.getPort();
    }
}