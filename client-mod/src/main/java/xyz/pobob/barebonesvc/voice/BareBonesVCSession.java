package xyz.pobob.barebonesvc.voice;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.config.ServerConfig;
import de.maxhenkel.voicechat.debug.CooldownTimer;
import de.maxhenkel.voicechat.net.PlayerStatePacket;
import de.maxhenkel.voicechat.voice.client.AudioChannel;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.client.ClientVoicechat;
import de.maxhenkel.voicechat.voice.common.PlayerSoundPacket;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import xyz.pobob.barebonesvc.BareBonesVCClient;
import xyz.pobob.barebonesvc.gui.ClientList;
import xyz.pobob.barebonesvc.mixin.ClientVoicechatAccessor;
import xyz.pobob.barebonesvc.mixin.playerstate.ClientPlayerStateManagerInvoker;
import xyz.pobob.barebonesvc.net.*;
import xyz.pobob.barebonesvc.voice.thread.ClientHandshakeThread;
import xyz.pobob.barebonesvc.voice.thread.MiscThreads;

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

public class BareBonesVCSession {

    private final ServerAudioPacket serverAudioPacket = new ServerAudioPacket();
    private final ClientHelloPacket clientHelloPacket = new ClientHelloPacket();
    private final ServerHelloPacket serverHelloPacket = new ServerHelloPacket();
    private final ClientKeepAlivePacket clientKeepAlivePacket = new ClientKeepAlivePacket();
    private final ServerKeepAlivePacket serverKeepAlivePacket = new ServerKeepAlivePacket();
    private final ClientUpdatePlayerPacket clientUpdatePlayerPacket = new ClientUpdatePlayerPacket();
    private final ServerUpdatePlayerPacket serverUpdatePlayerPacket = new ServerUpdatePlayerPacket();
    private final ServerUpdateVoiceDistancePacket serverUpdateVoiceDistancePacket = new ServerUpdateVoiceDistancePacket();
    private final ServerPlayerLatencyPacket serverPlayerLatencyPacket = new ServerPlayerLatencyPacket();

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

    private static BareBonesVCSession instance;
    public static synchronized BareBonesVCSession instance() {
        if (instance == null) instance = new BareBonesVCSession();
        return instance;
    }

    private volatile boolean running = false;
    private volatile boolean resolved = false;

    public void start(String host, int port) {
        if (VoicechatClient.CLIENT_CONFIG.muteOnJoin.get()) {
            ClientManager.getPlayerStateManager().setMuted(true);
        }

        this.client = null;
        this.config = null;

        InetSocketAddress address = new InetSocketAddress(host, port);
        if (address.isUnresolved()) {
            BareBonesVCSession.invalidAddress();
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
            BareBonesVCClient.LOGGER.error("An error occurred while starting voice client", e);
            return;
        }
        this.running = true;

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

                    if (!Packet.checkSignature(data)) return;

                    if (this.config == null) {
                        if (data[2] == Packet.Type.SERVER_HELLO.id) {

                            this.serverHelloPacket.deserialize(data);

                            ServerConfig.Codec codec = switch (this.serverHelloPacket.getCodec()) {
                                case VOIP -> ServerConfig.Codec.VOIP;
                                case AUDIO -> ServerConfig.Codec.AUDIO;
                                case RESTRICTED_LOWDELAY -> ServerConfig.Codec.RESTRICTED_LOWDELAY;
                            };

                            BareBonesVCClient.LOGGER.info(
                                    "Server config packet received! mojang auth={}, voice distance={}, codec={}, groups enabled={}",
                                    this.serverHelloPacket.getMojangAuth(),
                                    this.serverHelloPacket.getVoiceDistance(),
                                    codec,
                                    this.serverHelloPacket.getGroupsEnabled()
                            );

                            this.config = new SessionConfig(
                                    this.serverHelloPacket.getMojangAuth(),
                                    (float) this.serverHelloPacket.getVoiceDistance(),
                                    codec,
                                    this.serverHelloPacket.getGroupsEnabled()
                            );

                            this.lastKeepAlive = System.currentTimeMillis();
                            MiscThreads.startCheckingConnectionHealth();

                            if (this.serverHelloPacket.getMojangAuth()) {
                                // TODO add mojang auth
                                this.startVoiceChat();
                            } else {
                                this.startVoiceChat();
                            }

                        }
                    } else {
                        if (data[2] == Packet.Type.SERVER_AUDIO.id) {

                            if (this.client != null) {
                                this.serverAudioPacket.deserialize(data);
                                this.client.processSoundPacket(
                                        new PlayerSoundPacket(
                                                this.serverAudioPacket.getUUID(),
                                                this.serverAudioPacket.getUUID(),
                                                this.serverAudioPacket.getAudio(),
                                                this.serverAudioPacket.getSequenceNumber(),
                                                false,
                                                this.config.voiceDistance(),
                                                null
                                        )
                                );
                            }

                        } else if (data[2] == Packet.Type.SERVER_KEEP_ALIVE.id) {

                            if (this.isConnected()) {
                                this.lastKeepAlive = System.currentTimeMillis();

                                this.serverKeepAlivePacket.deserialize(data);
                                this.clientKeepAlivePacket.create(this.serverKeepAlivePacket.getId());
                                this.send(this.clientKeepAlivePacket.serialize());
                            }

                        } else if (data[2] == Packet.Type.SERVER_UPDATE_PLAYER.id) {

                            this.serverUpdatePlayerPacket.deserialize(data);
                            this.updatePlayerState();

                        } else if (data[2] == Packet.Type.SERVER_PLAYER_LATENCY.id) {

                            this.serverPlayerLatencyPacket.deserialize(data);
                            BareBonesVCClient.LATENCIES.put(
                                    this.serverPlayerLatencyPacket.getUUID(),
                                    this.serverPlayerLatencyPacket.getLatencyNano() * 1e-6
                            );

                        } else if (data[2] == Packet.Type.SERVER_UPDATE_VOICE_DISTANCE.id) {

                            this.serverUpdateVoiceDistancePacket.deserialize(data);
                            this.config.setVoiceDistance((float) this.serverUpdateVoiceDistancePacket.getVoiceDistance());

                        } else if (data[2] == Packet.Type.SERVER_KICK_PLAYER.id) {
                            if (MinecraftClient.getInstance().player != null) {
                                MinecraftClient.getInstance().player.sendMessage(Text.of("Kicked from Bare Bones VC server"), true);
                            }
                            this.disconnect();
                        } else if (data[2] == Packet.Type.SERVER_CLOSE.id) {

                            if (MinecraftClient.getInstance().player != null) {
                                MinecraftClient.getInstance().player.sendMessage(Text.of("Bare Bones VC server was stopped"), true);
                            }
                            this.disconnect();

                        }
                    }
                });
            }

            this.stopNow();
        });
        this.networkReceiveThread.setName("BareBonesVCNetworkThread");

        this.clientHandshakeThread.start();
        this.networkReceiveThread.start();

        BareBonesVCClient.LOGGER.info("Started connecting to voice server {}", BareBonesVCSession.instance().getReadableAddress());
    }

    private void startVoiceChat() {

        this.client = new ClientVoicechat();
        if (this.client.getMicThread() != null) {
            this.client.getMicThread().close();
        }
        ((ClientVoicechatAccessor) this.client).callStartMicThread(null);
        BareBonesVCClient.LOGGER.info("Starting microphone thread");

    }

    public synchronized boolean send(byte[] data) {
        if (!this.isRunning()) return false;

        this.sendPacket.setLength(data.length);
        System.arraycopy(data, 0, this.sendPacket.getData(), 0, data.length);

        try {
            this.socket.send(this.sendPacket);
            return true;
        } catch (IOException e) {
            BareBonesVCClient.LOGGER.error("An error occurred while sending Datagram packet", e);
            return false;
        }
    }

    public byte[] receive() throws IOException {
        if (!this.isRunning()) return null;

        this.socket.receive(this.recvPacket);

        if (this.recvPacket.getLength() >= this.recvPacket.getData().length) {
            CooldownTimer.run("udp_packet_too_large", () ->
                    BareBonesVCClient.LOGGER.warn("Packet from {} is too large", this.getReadableAddress()));
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

    private synchronized void updatePlayerState() {
        ((ClientPlayerStateManagerInvoker) ClientManager.getPlayerStateManager()).invokeUpdatePlayerState(
                null,
                new PlayerStatePacket(new PlayerState(
                        this.serverUpdatePlayerPacket.getUUID(),
                        this.serverUpdatePlayerPacket.getUsername(),
                        this.serverUpdatePlayerPacket.getDisabled(),
                        this.serverUpdatePlayerPacket.getDisconnected()
                ))
        );
        ClientList.update();
    }

    public void disconnect() {
        BareBonesVCClient.LOGGER.info("Disconnected from {}", this.getReadableAddress());

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
        return BareBonesVCSession.instance().client.getAudioChannels();
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