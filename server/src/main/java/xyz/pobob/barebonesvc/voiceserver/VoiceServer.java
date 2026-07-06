package xyz.pobob.barebonesvc.voiceserver;

import xyz.pobob.barebonesvc.BareBonesVCServer;
import xyz.pobob.barebonesvc.cli.command.*;
import xyz.pobob.barebonesvc.net.*;
import xyz.pobob.barebonesvc.voiceserver.retransmission.ReliablePacketManager;
import xyz.pobob.barebonesvc.voiceserver.thread.LatencyManager;
import xyz.pobob.barebonesvc.voiceserver.thread.MiscNetworkThreads;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;

public class VoiceServer {

    private final ThreadLocal<ServerUpdatePlayerPacket> localServerUpdatePlayerPacket = ThreadLocal.withInitial(ServerUpdatePlayerPacket::new);
    private final ServerClosePacket serverClosePacket = new ServerClosePacket();

    private final byte[] recvBuf = new byte[4096];
    private final byte[] sendBuf = new byte[4096];
    private final DatagramPacket recvPacket = new DatagramPacket(this.recvBuf, this.recvBuf.length);
    private final DatagramPacket sendPacket = new DatagramPacket(this.sendBuf, 0);

    public final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final ExecutorService pool = Executors.newFixedThreadPool(4);
    private DatagramSocket socket;

    public final Map<SocketAddress, ClientConnection> connected = new ConcurrentHashMap<>();
    public final Config config;
    public final ClientMessageDispatcher clientMessageDispatcher = new ClientMessageDispatcher();
    public final LatencyManager latencyManager = new LatencyManager(this);
    public final ReliablePacketManager reliablePacketManager = new ReliablePacketManager(this);

    public synchronized boolean isRunning() {
        return this.socket != null;
    }

    public VoiceServer(Config config) {
        this.config = config;
    }

    public void start() {
        try {
            this.socket = new DatagramSocket(this.config.port, this.config.listenAddress);
        } catch (Exception e) {
            BareBonesVCServer.LOGGER.log(Level.SEVERE, "An error occurred while starting voice server", e);
        }

        this.clientMessageDispatcher.register(PacketType.CLIENT_HELLO, new ClientHelloHandler(this));
        this.clientMessageDispatcher.register(PacketType.CLIENT_KEEP_ALIVE, new ClientKeepAliveHandler(this));
        this.clientMessageDispatcher.register(PacketType.CLIENT_AUDIO, new ClientAudioHandler(this));
        this.clientMessageDispatcher.register(PacketType.CLIENT_ACK, new ClientAckHandler(this));
        this.clientMessageDispatcher.register(PacketType.CLIENT_UPDATE_PLAYER, new ClientUpdatePlayerHandler(this));

        CommandDispatcher commandDispatcher = new CommandDispatcher();
        commandDispatcher.register("stop", new StopCommand(this));
        commandDispatcher.register("list", new ListCommand(this));
        commandDispatcher.register("kick", new KickCommand(this));
        commandDispatcher.register("voicedistance", new VoiceDistanceCommand(this));

        Thread console = new Thread(new ConsoleListener(this, commandDispatcher));
        console.setName("ConsoleThread");
        console.setDaemon(false);
        console.start();

        this.reliablePacketManager.start();
        MiscNetworkThreads.startKeepAliveThread(this);

        Thread networkThread = new Thread(() -> {
            while (this.isRunning()) {
                final byte[] data;
                try {
                    data = this.receive();
                    if (data.length < 5) continue;
                } catch (IOException e) {
                    continue;
                }

                final SocketAddress clientAddress = this.recvPacket.getSocketAddress();

                this.pool.submit(() -> {
                    if (Packet.checkSignature(data)) {
                        if (Packet.isReliable(data)) {
                            this.reliablePacketManager.receive(data, clientAddress);
                        } else {
                            this.clientMessageDispatcher.dispatch(data, clientAddress);
                        }
                    }
                });
            }
            this.close();
        });

        networkThread.setDaemon(true);
        networkThread.setName("BareBonesVCNetworkThread");
        networkThread.start();
    }

    public void announceExcluding(Packet packet, SocketAddress src) {
        for (SocketAddress address : this.connected.keySet()) {
            if (!Objects.equals(address, src)) {
                this.send(packet, address);
            }
        }
    }

    public void announce(Packet packet) {
        for (SocketAddress address : this.connected.keySet()) {
            this.send(packet, address);
        }
    }

    public synchronized void send(Packet packet, SocketAddress clientAddress) {
        if (this.isRunning()) {
            if (packet instanceof ReliablePacket rp) {
                this.reliablePacketManager.registerSequence(rp, clientAddress);
            }

            byte[] data = packet.serialize();

            this.sendPacket.setLength(data.length);
            System.arraycopy(data, 0, this.sendPacket.getData(), 0, data.length);
            this.sendPacket.setSocketAddress(clientAddress);

            try {
                this.socket.send(this.sendPacket);
            } catch (IOException e) {
                BareBonesVCServer.LOGGER.log(Level.SEVERE, "An error occurred while sending Datagram packet", e);
            }
        } else {
            BareBonesVCServer.LOGGER.warning("Unable to send packet to " + this.sendPacket.getSocketAddress() + " because socket is not open yet");
        }
    }

    public byte[] receive() throws IOException {
        this.socket.receive(this.recvPacket);

        if (this.recvPacket.getLength() >= this.recvPacket.getData().length) {
            BareBonesVCServer.LOGGER.severe("Packet from " + this.recvPacket.getSocketAddress() + " is too large");
        }

        byte[] data = new byte[this.recvPacket.getLength()];
        System.arraycopy(this.recvPacket.getData(), this.recvPacket.getOffset(), data, 0, this.recvPacket.getLength());

        return data;
    }

    public void onDisconnect(SocketAddress clientAddress) {
        ClientConnection disconnected = this.connected.remove(clientAddress);
        if (disconnected != null) {
            this.localServerUpdatePlayerPacket.get().create(
                    disconnected.getUsername(),
                    disconnected.getUUID(),
                    false,
                    true
            );
            BareBonesVCServer.LOGGER.info("Client disconnected: " + disconnected.getUsername() + " (" + disconnected.getUUID() + ")");
        }
    }

    public void onTimeout(SocketAddress clientAddress) {
        ClientConnection connection = this.connected.get(clientAddress);
        if (connection != null) {
            BareBonesVCServer.LOGGER.info(connection.getUsername() + " timed out!");
            this.onDisconnect(clientAddress);
        }
    }

    public void close() {
        for (SocketAddress address : this.connected.keySet()) {
            this.send(this.serverClosePacket, address);
        }
        this.stopNow();
    }

    public void stopNow() {
        if (this.isRunning()) {
            this.socket.close();
        }
    }
}
