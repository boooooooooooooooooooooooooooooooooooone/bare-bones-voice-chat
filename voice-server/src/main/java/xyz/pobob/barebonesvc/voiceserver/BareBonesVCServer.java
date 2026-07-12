package xyz.pobob.barebonesvc.voiceserver;

import xyz.pobob.barebonesvc.BareBonesVC;
import xyz.pobob.barebonesvc.cli.command.CommandDispatcher;
import xyz.pobob.barebonesvc.cli.command.ConsoleListener;
import xyz.pobob.barebonesvc.packet.Packet;
import xyz.pobob.barebonesvc.packet.ReliablePacket;
import xyz.pobob.barebonesvc.packet.ServerClosePacket;
import xyz.pobob.barebonesvc.packet.ServerUpdatePlayerPacket;
import xyz.pobob.barebonesvc.packet.registry.PacketRegistry;
import xyz.pobob.barebonesvc.packet.retransmission.ReliablePacketManager;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;

public class BareBonesVCServer {

    private final ThreadLocal<ServerUpdatePlayerPacket> localServerUpdatePlayerPacket = ThreadLocal.withInitial(ServerUpdatePlayerPacket::new);
    private final ServerClosePacket serverClosePacket = new ServerClosePacket();

    private final byte[] recvBuf = new byte[4096];
    private final byte[] sendBuf = new byte[4096];
    private final DatagramPacket recvPacket = new DatagramPacket(this.recvBuf, this.recvBuf.length);
    private final DatagramPacket sendPacket = new DatagramPacket(this.sendBuf, 0);

    public final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final ExecutorService pool = Executors.newFixedThreadPool(4);
    public final ExecutorService ioThread = Executors.newSingleThreadExecutor();
    private DatagramSocket socket;

    private final Map<SocketAddress, ClientConnection> connected = new ConcurrentHashMap<>();

    public synchronized void addClient(SocketAddress clientAddress, ClientConnection connection) {
        this.connected.put(clientAddress, connection);
    }

    public synchronized ClientConnection removeClient(SocketAddress clientAddress) {
        return this.connected.remove(clientAddress);
    }

    public synchronized ClientConnection getClient(SocketAddress clientAddress) {
        return this.connected.get(clientAddress);
    }

    public synchronized ClientConnection getAuthenticatedClient(SocketAddress clientAddress) {
        ClientConnection connection = this.connected.get(clientAddress);
        return (connection != null && connection.isAuthenticated()) ? connection : null;
    }

    public synchronized Collection<ClientConnection> getAuthenticatedClients() {
        return this.connected.values()
                .stream().filter(ClientConnection::isAuthenticated).toList();
    }

    public Collection<Map.Entry<SocketAddress, ClientConnection>> getAuthenticatedEntries() {
        return this.connected.entrySet()
                .stream().filter(entry -> entry.getValue().isAuthenticated()).toList();
    }

    public synchronized Collection<SocketAddress> getAuthenticatedSockets() {
        return this.getAuthenticatedEntries().stream().map(Map.Entry::getKey).toList();
    }

    public synchronized int getConnectedCount() {
        return this.getAuthenticatedClients().size();
    }

    public synchronized boolean isSocketConnected(SocketAddress clientAddress) {
        return this.connected.containsKey(clientAddress);
    }

    public final LatencyManager latencyManager = new LatencyManager(this);
    public final ReliablePacketManager reliablePacketManager = new ReliablePacketManager(this);
    public final Config config;
    public KeyPair keyPair;

    public void generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(1024);
        this.keyPair = generator.generateKeyPair();
    }

    public boolean isRunning() {
        return this.socket != null;
    }

    public BareBonesVCServer(Config config) {
        this.config = config;
    }

    public void start() {
        try {
            this.socket = new DatagramSocket(this.config.port, this.config.listenAddress);
        } catch (Exception e) {
            BareBonesVC.LOGGER.log(Level.SEVERE, "An error occurred while starting voice server", e);
        }

        Thread consoleThread = new Thread(
                null,
                new ConsoleListener(this, new CommandDispatcher(this)),
                "ConsoleThread"
        );
        consoleThread.setDaemon(false);
        consoleThread.start();

        Thread networkReceiveThread = new Thread(null, () -> {
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
                            PacketRegistry.dispatchClientPacket(data, clientAddress);
                        }
                    }
                });
            }
            this.close();
        }, "BareBonesVCNetworkThread");
        networkReceiveThread.setDaemon(false);
        networkReceiveThread.start();

        MiscTasks.startKeepAliveTask(this);
        this.reliablePacketManager.startCheckingPendingPackets();

        BareBonesVC.LOGGER.info("Bare Bones Voice Chat server started");
    }

    public void announceExcluding(Packet packet, SocketAddress excluded) {
        for (SocketAddress address : this.getAuthenticatedSockets()) {
            if (!Objects.equals(address, excluded)) {
                this.send(packet, address);
            }
        }
    }

    public void announce(Packet packet) {
        for (SocketAddress address : this.getAuthenticatedSockets()) {
            this.send(packet, address);
        }
    }

    public void send(Packet packet, SocketAddress clientAddress) {
        if (packet instanceof ReliablePacket rp) {
            this.reliablePacketManager.registerSequence(rp, clientAddress);
        }

        this.send(packet.serialize(), clientAddress);
    }

    public synchronized void send(byte[] data, SocketAddress clientAddress) {
        this.sendPacket.setLength(data.length);
        System.arraycopy(data, 0, this.sendPacket.getData(), 0, data.length);
        this.sendPacket.setSocketAddress(clientAddress);

        try {
            this.socket.send(this.sendPacket);
        } catch (IOException e) {
            BareBonesVC.LOGGER.log(Level.SEVERE, "An error occurred while sending Datagram packet", e);
        }
    }

    public byte[] receive() throws IOException {
        this.socket.receive(this.recvPacket);

        if (this.recvPacket.getLength() >= this.recvPacket.getData().length) {
            BareBonesVC.LOGGER.severe("Packet from " + this.recvPacket.getSocketAddress() + " is too large");
        }

        byte[] data = new byte[this.recvPacket.getLength()];
        System.arraycopy(this.recvPacket.getData(), this.recvPacket.getOffset(), data, 0, this.recvPacket.getLength());

        return data;
    }

    public void onAuthenticated(SocketAddress clientAddress, ClientConnection clientConnection) {
        if (clientConnection.isAuthenticated()) return;

        BareBonesVC.LOGGER.info("Client connected: " + clientConnection.getUsername() + " (" + clientConnection.getUUID() + ")");

        clientConnection.setAuthenticated(true);

        ServerUpdatePlayerPacket serverUpdatePlayerPacket = new ServerUpdatePlayerPacket();

        for (ClientConnection c : this.getAuthenticatedClients()) { // introduce already present players to new client
            serverUpdatePlayerPacket.create(
                    c.getUsername(),
                    c.getUUID(),
                    c.isDisabled(),
                    false,
                    false
            );
            this.send(serverUpdatePlayerPacket, clientAddress);
        }

        this.localServerUpdatePlayerPacket.get().create( // announce new client
                clientConnection.getUsername(),
                clientConnection.getUUID(),
                clientConnection.isDisabled(),
                false,
                true
        );
        this.announceExcluding(this.localServerUpdatePlayerPacket.get(), clientAddress);
    }

    public void onDisconnect(SocketAddress clientAddress) {
        ClientConnection disconnected = this.removeClient(clientAddress);
        if (disconnected != null) {
            this.localServerUpdatePlayerPacket.get().create(
                    disconnected.getUsername(),
                    disconnected.getUUID(),
                    false,
                    true,
                    true
            );
            this.announce(this.localServerUpdatePlayerPacket.get());
            BareBonesVC.LOGGER.info("Client disconnected: " + disconnected.getUsername() + " (" + disconnected.getUUID() + ")");
        }
    }

    public void onTimeout(SocketAddress clientAddress) {
        ClientConnection connection = this.getClient(clientAddress);
        if (connection != null) {
            BareBonesVC.LOGGER.info(connection.getUsername() + " timed out!");
            this.onDisconnect(clientAddress);
        }
    }

    public void close() {
        for (SocketAddress address : this.getAuthenticatedSockets()) {
            this.send(this.serverClosePacket, address);
        }
        this.stopNow();
    }

    public void stopNow() {
        if (this.isRunning()) {
            this.socket.close();
        }
        this.pool.shutdown();
        this.scheduler.shutdown();
    }
}
