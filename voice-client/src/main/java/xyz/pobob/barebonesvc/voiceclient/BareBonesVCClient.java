package xyz.pobob.barebonesvc.voiceclient;

import xyz.pobob.barebonesvc.packet.ClientUpdatePlayerPacket;
import xyz.pobob.barebonesvc.packet.Packet;
import xyz.pobob.barebonesvc.packet.handler.ServerHelloHandler;
import xyz.pobob.barebonesvc.packet.registry.PacketRegistry;
import xyz.pobob.barebonesvc.packet.retransmission.ReliablePacket;
import xyz.pobob.barebonesvc.packet.retransmission.ReliablePacketManager;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

public abstract class BareBonesVCClient {

    public static BareBonesVCClient INSTANCE;
    public static final int TIMEOUT_MILLIS = 20000;

    private final ClientUpdatePlayerPacket clientUpdatePlayerPacket = new ClientUpdatePlayerPacket();

    private final byte[] recvBuf = new byte[4096];
    private final DatagramPacket recvPacket = new DatagramPacket(this.recvBuf, this.recvBuf.length);
    private final byte[] sendBuf = new byte[4096];
    private final DatagramPacket sendPacket = new DatagramPacket(this.sendBuf, 0);

    public ScheduledExecutorService scheduler;
    private ExecutorService pool;
    private DatagramSocket socket;

    public final Map<UUID, Double> latencies = new ConcurrentHashMap<>();
    public ReliablePacketManager reliablePacketManager;

    public volatile SessionConfig config;
    public String host;
    public int port;
    public long lastKeepAlive = -1;

    private volatile boolean running = false;

    public void start(String host, int port) {
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.pool = Executors.newSingleThreadExecutor();

        InetSocketAddress address = new InetSocketAddress(host, port);
        if (address.isUnresolved()) {
            this.sendMessage("Failed to resolve address", true);
            return;
        } else {
            this.recvPacket.setSocketAddress(address);
            this.sendPacket.setSocketAddress(address);
        }

        try {
            this.socket = new DatagramSocket();
        } catch (SocketException e) {
            this.logError("An error occurred while starting voice client", e);
            return;
        }
        this.running = true;

        this.shutdownVoiceChat();

        Thread networkReceiveThread = new Thread(null, () -> {
            while (this.isRunning()) {
                final byte[] data;
                try {
                    data = this.receive();
                    if (data.length < 5) continue;
                } catch (IOException e) {
                    continue;
                } catch (Exception e) {
                    break;
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
        }, "BareBonesVCNetworkThread");
        networkReceiveThread.setDaemon(false);
        networkReceiveThread.start();

        this.reliablePacketManager = new ReliablePacketManager();
        this.reliablePacketManager.startCheckingPendingPackets();

        MiscTasks.startHandshake();

        this.logInfo("Started connecting to voice server " + this.getReadableAddress());
        this.sendFeed("Connecting to voice server...");
    }

    public void send(Packet packet) {
        if (packet instanceof ReliablePacket rp) {
            this.reliablePacketManager.registerSequence(rp);
        }

        this.send(packet.serialize());
    }

    public synchronized void send(byte[] data) {
        if (this.isRunning()) {
            this.sendPacket.setLength(data.length);
            System.arraycopy(data, 0, this.sendPacket.getData(), 0, data.length);

            try {
                if (this.socket != null && !this.socket.isClosed()) {
                    this.socket.send(this.sendPacket);
                }
            } catch (IOException e) {
                this.logError("An error occurred while sending Datagram packet", e);
            }
        }
    }

    public byte[] receive() throws IOException {
        if (this.socket != null && !this.socket.isClosed()) {
            this.socket.receive(this.recvPacket);
        }

        if (this.recvPacket.getLength() >= this.recvPacket.getData().length) {
            throw new IOException("Packet from" + this.getReadableAddress() + "is too large");
        }

        byte[] data = new byte[this.recvPacket.getLength()];
        System.arraycopy(this.recvPacket.getData(), this.recvPacket.getOffset(), data, 0, this.recvPacket.getLength());
        return data;
    }

    public void onAuthenticated() {
        BareBonesVCClient.INSTANCE.lastKeepAlive = System.currentTimeMillis();
        this.startOurSVC();

        this.sendMessage("Successfully connected to Bare Bones VC server!", true);
        this.sendFeed(this.getOwnUsername() + " joined");
    }

    public void onTimeout() {
        this.sendMessage("Bare Bones VC connection timed out", true);
        this.onDisconnect(true);
    }

    public void onDisconnect(boolean notQuitting) {
        this.logInfo("Disconnected from " + this.getReadableAddress());
        this.clearFeed();

        if (this.isConnected()) {
            this.clientUpdatePlayerPacket.create(false, true);
            this.send(this.clientUpdatePlayerPacket);
        }

        this.close(notQuitting);
    }

    public synchronized void close(boolean notQuitting) {
        this.running = false;

        try {
            if (this.scheduler != null) {
                this.scheduler.shutdown();
                this.scheduler.awaitTermination(2, TimeUnit.SECONDS);
            }
            if (this.pool != null) {
                this.pool.shutdown();
                this.pool.awaitTermination(2, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        this.shutdownVoiceChat();

        if (notQuitting) {
            this.startVanillaSVC();
        }

        if (this.socket != null) {
            this.socket.close();
            this.socket = null;
        }

        if (this.reliablePacketManager != null) {
            this.reliablePacketManager.clear();
        }
        ServerHelloHandler.waitingForServerHello = true;
        this.lastKeepAlive = -1;
        this.config = null;
        this.latencies.clear();
    }

    public boolean isRunning() {
        return this.running;
    }

    public boolean isConnected() {
        return this.isRunning() && System.currentTimeMillis() - this.lastKeepAlive < TIMEOUT_MILLIS;
    }

    public String getReadableAddress() {
        return ((this.sendPacket.getAddress() == null) ? "null" : this.sendPacket.getAddress().getHostAddress()) + ":" + this.sendPacket.getPort();
    }

    public abstract String getOwnUsername();

    public abstract UUID getOwnUUID();

    public abstract boolean getOwnDisabled();

    public abstract void logInfo(String msg);

    public abstract void logWarn(String msg);

    public abstract void logError(String msg, Throwable t);

    public abstract void sendMessage(String message, boolean overlay);

    public abstract void sendFeed(String message);

    public abstract void clearFeed();

    public abstract void startVanillaSVC();

    public abstract void startOurSVC();

    public abstract void shutdownVoiceChat();

    public abstract void passSoundPacketToSVC(byte[] audio, long sequenceNumber, UUID uuid, boolean whispering);

    public abstract void updatePlayerState(UUID uuid, String username, boolean disabled, boolean disconnected);

    public abstract void clearPlayerStates();

    public abstract void registerClientQuitEvent(Runnable action);

    public abstract Executor getIoWorkerExecutor();

    public abstract String getDigest(byte[] publicKey) throws Exception;

    public abstract boolean requestSessionServerJoin(String digest);
}