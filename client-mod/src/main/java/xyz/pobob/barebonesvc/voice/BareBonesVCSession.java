package xyz.pobob.barebonesvc.voice;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.config.ServerConfig;
import de.maxhenkel.voicechat.debug.CooldownTimer;
import de.maxhenkel.voicechat.voice.client.AudioChannel;
import de.maxhenkel.voicechat.voice.client.ChatUtils;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.client.ClientVoicechat;
import de.maxhenkel.voicechat.voice.common.PlayerSoundPacket;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import xyz.pobob.barebonesvc.BareBonesVCClient;
import xyz.pobob.barebonesvc.net.*;
import xyz.pobob.barebonesvc.voice.thread.ClientHandshake;
import xyz.pobob.barebonesvc.voice.thread.MicThread;
import xyz.pobob.barebonesvc.voice.thread.MiscNetworkThreads;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BareBonesVCSession {

    private final ServerAudioPacket serverAudioPacket = new ServerAudioPacket();
    private final ServerHelloPacket serverHelloPacket = new ServerHelloPacket();
    private final ClientHelloPacket clientHelloPacket = new ClientHelloPacket();
    private final ClientUpdatePlayerPacket clientUpdatePlayerPacket = new ClientUpdatePlayerPacket();
    private final ServerUpdatePlayerPacket serverUpdatePlayerPacket = new ServerUpdatePlayerPacket();

    private final byte[] recvBuf = new byte[4096];
    private final DatagramPacket recvPacket = new DatagramPacket(recvBuf, recvBuf.length);
    private final byte[] sendBuf = new byte[4096];
    private final DatagramPacket sendPacket = new DatagramPacket(sendBuf, 0);

    private final ExecutorService pool = Executors.newSingleThreadExecutor();

    private DatagramSocket socket;

    private static BareBonesVCSession instance;

    public static synchronized BareBonesVCSession instance() {
        if (instance == null) instance = new BareBonesVCSession();
        return instance;
    }

    private volatile boolean running = false;
    private volatile boolean resolved = false;

    public ClientVoicechat client;
    public MicThread micThread;
    public SessionConfig config;
    public long lastKeepAlive = 0;

    public void start(String host, int port) {

        this.client = null;
        this.micThread = null;
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
                ClientManager.getPlayerStateManager().isDisabled()
        );
        ClientHandshake clientHandshake = new ClientHandshake(this.clientHelloPacket.serialize());

        Thread networkThread = new Thread(() -> {
            while (this.isRunning()) {
                final byte[] data;
                try {
                    data = this.receive();
                    if (data.length < 5) continue;
                } catch (IOException e) {
                    this.packetReadError();
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
                                    "Server config packet received! mojang auth={}, mtu={}, voice distance={}, codec={}, groups enabled={}",
                                    this.serverHelloPacket.getMojangAuth(),
                                    this.serverHelloPacket.getMtuSize(),
                                    this.serverHelloPacket.getVoiceDistance(),
                                    codec,
                                    this.serverHelloPacket.getGroupsEnabled()
                            );

                            this.config = new SessionConfig(
                                    this.serverHelloPacket.getMojangAuth(),
                                    this.serverHelloPacket.getMtuSize(),
                                    this.serverHelloPacket.getVoiceDistance(),
                                    codec,
                                    this.serverHelloPacket.getGroupsEnabled()
                            );

                            this.lastKeepAlive = System.currentTimeMillis();
                            MiscNetworkThreads.startSendingKeepAlives();
                            MiscNetworkThreads.startCheckingConnectionHealth();
                            MiscNetworkThreads.startUpdatingPlayerState();

                            if (this.serverHelloPacket.getMojangAuth()) {
                                // TODO add mojang auth
                            } else {
                                this.startVoiceChat();
                            }

                        }
                    } else {
                        if (data[2] == Packet.Type.SERVER_AUDIO.id) {

                            if (this.client == null) return;

                            this.serverAudioPacket.deserialize(data);
                            this.processSoundPacket(
                                    new PlayerSoundPacket(
                                            this.serverAudioPacket.getUUID(),
                                            this.serverAudioPacket.getUUID(),
                                            this.serverAudioPacket.getAudio(),
                                            this.serverAudioPacket.getSequenceNumber(),
                                            false,
                                            (float) this.config.voiceDistance(),
                                            null
                                    )
                            );

                        } else if (data[2] == Packet.Type.SERVER_KEEP_ALIVE.id) {

                            if (this.isConnected()) {
                                this.lastKeepAlive = System.currentTimeMillis();
                            }

                        } else if (data[2] == Packet.Type.SERVER_UPDATE_PLAYER.id) {

                            this.serverUpdatePlayerPacket.deserialize(data);
                            PlayerState state = new PlayerState(
                                    this.serverUpdatePlayerPacket.getUUID(),
                                    this.serverUpdatePlayerPacket.getUsername(),
                                    this.serverUpdatePlayerPacket.getDisabled(),
                                    this.serverUpdatePlayerPacket.getDisconnected()
                            );

                            PlayerStateInjector.updatePlayerState(state.getUuid(), state);

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
        networkThread.setName("BareBonesVCNetworkThread");

        clientHandshake.start();
        networkThread.start();

        BareBonesVCClient.LOGGER.info("Started connecting to voice server {}", BareBonesVCSession.instance().getReadableAddress());


    }

    private void startVoiceChat() {

        this.client = new ClientVoicechat();

        // create ClientVoicechatConnection instance to satisfy not-null checks
        // see ClientVoicechatMixin
        try {
            this.client.connect(null);
        } catch (Exception ignored) {}


        if (this.micThread != null) {
            this.micThread.close();
        }
        this.micThread = new MicThread(this.client, e -> {
            BareBonesVCClient.LOGGER.error("Failed to start microphone thread", e);
            ChatUtils.sendModErrorMessage("message.voicechat.microphone_unavailable", e);
        });
        micThread.start();
        BareBonesVCClient.LOGGER.info("Starting microphone thread");

    }

    private void processSoundPacket(PlayerSoundPacket packet) {
        synchronized (this.client.getAudioChannels()) {
            if (!ClientManager.getPlayerStateManager().isDisabled()) {
                AudioChannel sendTo = this.client.getAudioChannels().get(packet.getChannelId());
                if (sendTo == null) {
                    try {
                        AudioChannel ch = new AudioChannel(this.client, null, packet.getChannelId());
                        ch.addToQueue(packet);
                        ch.start();
                        this.client.getAudioChannels().put(packet.getChannelId(), ch);
                    } catch (Exception e) {
                        CooldownTimer.run("playback_unavailable", () -> {
                            Voicechat.LOGGER.error("Failed to create audio channel", e);
                            ChatUtils.sendModErrorMessage("message.voicechat.playback_unavailable", e);
                        });
                    }
                } else {
                    sendTo.addToQueue(packet);
                }
            }

            this.client.getAudioChannels().values().stream().filter(AudioChannel::canKill).forEach(AudioChannel::closeAndKill);
            this.client.getAudioChannels().entrySet().removeIf((entry) -> (entry.getValue()).isClosed());
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

    public void disconnect() {
        BareBonesVCClient.LOGGER.info("Disconnected from {}", this.getReadableAddress());

        if (this.isRunning()) {
            this.clientUpdatePlayerPacket.create(ClientManager.getPlayerStateManager().isDisabled(), true);
            this.send(this.clientUpdatePlayerPacket.serialize());
        }

        this.client.close();
        this.client = null;

        if (this.micThread != null) {
            this.micThread.close();
            this.micThread = null;
        }

        this.stopNow();
    }

    public void stopNow() {
        if (this.isRunning()) {
            this.running = false;
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
        return this.running && System.currentTimeMillis() - this.lastKeepAlive < 30000;
    }

    public static void invalidAddress() {
        if (MinecraftClient.getInstance().player == null) return;
        MinecraftClient.getInstance().player.sendMessage(Text.of("Failed to resolve address"), true);
    }

    public void packetReadError() {
        BareBonesVCClient.LOGGER.error("An error occurred while reading packet from {}", this.getReadableAddress());
    }

    public String getReadableAddress() {
        return ((this.sendPacket.getAddress() == null) ? "null" : this.sendPacket.getAddress().getHostAddress()) + ":" + this.sendPacket.getPort();
    }
}