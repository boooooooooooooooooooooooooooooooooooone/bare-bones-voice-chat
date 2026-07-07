package xyz.pobob.barebonesvc.voiceclient;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.debug.CooldownTimer;
import de.maxhenkel.voicechat.voice.client.AudioChannel;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.client.ClientVoicechat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import xyz.pobob.barebonesvc.BareBonesVC;
import xyz.pobob.barebonesvc.mixin.ClientVoicechatAccessor;
import xyz.pobob.barebonesvc.packet.ClientUpdatePlayerPacket;
import xyz.pobob.barebonesvc.packet.Packet;
import xyz.pobob.barebonesvc.packet.ReliablePacket;
import xyz.pobob.barebonesvc.packet.registry.PacketRegistry;
import xyz.pobob.barebonesvc.packet.retransmission.ReliablePacketManager;

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

public class BareBonesVCClient {

    public static final BareBonesVCClient INSTANCE = new BareBonesVCClient();
    public static final int TIMEOUT_MILLIS = 20000;

    private final ClientUpdatePlayerPacket clientUpdatePlayerPacket = new ClientUpdatePlayerPacket();

    private final byte[] recvBuf = new byte[4096];
    private final DatagramPacket recvPacket = new DatagramPacket(recvBuf, recvBuf.length);
    private final byte[] sendBuf = new byte[4096];
    private final DatagramPacket sendPacket = new DatagramPacket(sendBuf, 0);

    public ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final ExecutorService pool = Executors.newSingleThreadExecutor();
    private DatagramSocket socket;

    public ReliablePacketManager reliablePacketManager;

    public ClientVoicechat client;
    public SessionConfig config;
    public long lastKeepAlive = 0;

    private boolean running = false;
    private boolean resolved = false;

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

        Thread networkReceiveThread = new Thread(null, () -> {
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
                        if (Packet.isReliable(data)) {
                            this.reliablePacketManager.receive(data);
                        } else {
                            PacketRegistry.dispatchServerPacket(data);
                        }
                    }
                });
            }

            this.stopNow();
        }, "BareBonesVCNetworkThread");
        networkReceiveThread.setDaemon(false);
        networkReceiveThread.start();

        MiscTasks.startHandshake();

        this.reliablePacketManager = new ReliablePacketManager();
        this.reliablePacketManager.start();

        BareBonesVC.LOGGER.info("Started connecting to voice server {}", this.getReadableAddress());
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

    public synchronized void send(Packet packet) {
        if (this.isRunning()) {
            if (packet instanceof ReliablePacket rp) {
                this.reliablePacketManager.registerSequence(rp);
            }

            byte[] data = packet.serialize();

            this.sendPacket.setLength(data.length);
            System.arraycopy(data, 0, this.sendPacket.getData(), 0, data.length);

            try {
                this.socket.send(this.sendPacket);
            } catch (IOException e) {
                BareBonesVC.LOGGER.error("An error occurred while sending Datagram packet", e);
            }
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

    public void onDisconnect() {
        BareBonesVC.LOGGER.info("Disconnected from {}", this.getReadableAddress());

        if (this.isRunning()) {
            this.clientUpdatePlayerPacket.create(ClientManager.getPlayerStateManager().isDisabled(), true);
            this.send(this.clientUpdatePlayerPacket);
        }
        MinecraftClient.getInstance().execute(() -> ClientManager.getPlayerStateManager().clearStates());

        if (this.client != null) {
            this.client.closeMicThread();
            this.client.close();
            this.client = null;
        }

        this.stopNow();
    }

    public void onTimeout() {
        if (MinecraftClient.getInstance().player != null) {
            MinecraftClient.getInstance().player.sendMessage(Text.of("Bare Bones VC connection timed out"), true);
        }
        this.onDisconnect();
    }

    public void stopNow() {
        this.scheduler.shutdown();

        if (this.reliablePacketManager != null) {
            this.reliablePacketManager.clear();
        }

        if (this.isRunning()) {
            this.running = false;
            this.resolved = false;
        }

        if (this.socket != null) {
            this.socket.close();
            this.socket = null;
        }
    }

    public boolean isRunning() {
        return this.running && this.resolved;
    }

    public boolean isConnected() {
        return this.running && System.currentTimeMillis() - this.lastKeepAlive < TIMEOUT_MILLIS;
    }

    public synchronized Map<UUID, AudioChannel> getAudioChannels() {
        return this.client.getAudioChannels();
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