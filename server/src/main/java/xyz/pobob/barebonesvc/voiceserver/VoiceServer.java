package xyz.pobob.barebonesvc.voiceserver;

import xyz.pobob.barebonesvc.BareBonesVCServer;
import xyz.pobob.barebonesvc.cli.command.*;
import xyz.pobob.barebonesvc.net.*;
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

    private final ServerClosePacket serverClosePacket = new ServerClosePacket();

    private final byte[] recvBuf = new byte[4096];
    private final byte[] sendBuf = new byte[4096];
    private final DatagramPacket recvPacket = new DatagramPacket(this.recvBuf, this.recvBuf.length);
    private final DatagramPacket sendPacket = new DatagramPacket(this.sendBuf, 0);

    private final ExecutorService pool = Executors.newFixedThreadPool(4);
    public final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private DatagramSocket socket;

    public final Map<SocketAddress, ClientConnection> connected = new ConcurrentHashMap<>();
    public final Config config;
    public final LatencyManager latencyManager = new LatencyManager(this);

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

        final ClientMessageDispatcher clientMessageDispatcher = new ClientMessageDispatcher();
        clientMessageDispatcher.register(PacketType.CLIENT_HELLO, new ClientHelloHandler(this));
        clientMessageDispatcher.register(PacketType.CLIENT_KEEP_ALIVE, new ClientKeepAliveHandler(this));
        clientMessageDispatcher.register(PacketType.CLIENT_AUDIO, new ClientAudioHandler(this));
        clientMessageDispatcher.register(PacketType.CLIENT_UPDATE_PLAYER, new ClientUpdatePlayerHandler(this));

        CommandDispatcher commandDispatcher = new CommandDispatcher();
        commandDispatcher.register("stop", new StopCommand(this));
        commandDispatcher.register("list", new ListCommand(this));
        commandDispatcher.register("kick", new KickCommand(this));
        commandDispatcher.register("voicedistance", new VoiceDistanceCommand(this));

        Thread console = new Thread(new ConsoleListener(this, commandDispatcher));
        console.setName("ConsoleThread");
        console.setDaemon(false);
        console.start();

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
                        clientMessageDispatcher.dispatch(data, clientAddress);
                    }
                });
            }
            this.close();
        });

        networkThread.setDaemon(true);
        networkThread.setName("BareBonesNetworkThread");
        networkThread.start();

    }

    public void announceExcluding(byte[] data, SocketAddress src) {
        for (SocketAddress address : this.connected.keySet()) {
            if (!Objects.equals(address, src)) {
                this.send(data, address);
            }
        }
    }

    public void announce(byte[] data) {
        for (SocketAddress address : this.connected.keySet()) {
            this.send(data, address);
        }
    }

    public synchronized boolean send(byte[] data, SocketAddress address) {
        if (this.isRunning()) {

            this.sendPacket.setLength(data.length);
            System.arraycopy(data, 0, this.sendPacket.getData(), 0, data.length);
            this.sendPacket.setSocketAddress(address);
            try {
                this.socket.send(this.sendPacket);
                return true;
            } catch (IOException e) {
                BareBonesVCServer.LOGGER.log(Level.SEVERE, "An error occurred while sending Datagram packet", e);
                return false;
            }

        } else {
            BareBonesVCServer.LOGGER.warning("Unable to send packet to " + this.sendPacket.getSocketAddress() + " because server is not running");
            return false;
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

    public void close() {
        for (SocketAddress address : this.connected.keySet()) {
            this.send(this.serverClosePacket.serialize(), address);
        }
        this.stopNow();
    }

    public void stopNow() {
        if (this.isRunning()) {
            this.socket.close();
        }
    }

    public String getReadableAddress(DatagramPacket packet) {
        return ((packet.getAddress() == null) ? "null" : packet.getAddress().getHostAddress()) + ":" + packet.getPort();
    }
}
