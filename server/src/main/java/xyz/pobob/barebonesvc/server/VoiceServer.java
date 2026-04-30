package xyz.pobob.barebonesvc.server;

import xyz.pobob.barebonesvc.BareBonesVCServer;
import xyz.pobob.barebonesvc.Config;
import xyz.pobob.barebonesvc.cli.command.CommandDispatcher;
import xyz.pobob.barebonesvc.cli.command.ConsoleListener;
import xyz.pobob.barebonesvc.cli.command.ListCommand;
import xyz.pobob.barebonesvc.cli.command.StopCommand;
import xyz.pobob.barebonesvc.net.*;
import xyz.pobob.barebonesvc.server.thread.ServerKeepAliveThreads;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

public class VoiceServer {

    private final ThreadLocal<ClientHelloPacket> localClientHelloPacket = ThreadLocal.withInitial(ClientHelloPacket::new);
    private final ThreadLocal<ServerHelloPacket> localServerHelloPacket = ThreadLocal.withInitial(ServerHelloPacket::new);
    private final ThreadLocal<ClientAudioPacket> localClientAudioPacket = ThreadLocal.withInitial(ClientAudioPacket::new);
    private final ThreadLocal<ServerAudioPacket> localServerAudioPacket = ThreadLocal.withInitial(ServerAudioPacket::new);

    private final ServerDisconnectPacket serverDisconnectPacket = new ServerDisconnectPacket();

    private final byte[] recvBuf = new byte[4096];
    private final DatagramPacket recvPacket = new DatagramPacket(this.recvBuf, this.recvBuf.length);
    private final byte[] sendBuf = new byte[4096];
    private final DatagramPacket sendPacket = new DatagramPacket(this.sendBuf, 0);

    private final ExecutorService pool = Executors.newFixedThreadPool(4);
    private DatagramSocket socket;

    public final Config config;

    public synchronized boolean isRunning() {
        return socket != null;
    }

    public final Map<SocketAddress, ClientConnection> connected = new ConcurrentHashMap<>();

    public VoiceServer(Config config) {
        this.config = config;
    }

    public void start() {
        try {
            this.socket = new DatagramSocket(this.config.port, this.config.listenAddress);
        } catch (Exception e) {
            BareBonesVCServer.LOGGER.log(Level.SEVERE, "An error occurred while starting voice server", e);
        }

        CommandDispatcher dispatcher = new CommandDispatcher();
        dispatcher.register("stop", new StopCommand(this));
        dispatcher.register("list", new ListCommand(this));
        Thread console = new Thread(new ConsoleListener(this, dispatcher));

        console.setName("ConsoleThread");
        console.setDaemon(true);
        console.start();

        ServerKeepAliveThreads.startSending(this);
        ServerKeepAliveThreads.startCheckingConnectionHealth(this);

        Thread networkThread = new Thread(() -> {
            while (this.isRunning()) {
                final byte[] data;
                try {
                    data = receive();
                    if (data.length < 5) continue;
                } catch (IOException e) {
                    continue;
                }

                final SocketAddress clientAddress = this.recvPacket.getSocketAddress();
                final String readableAddress = this.getReadableAddress(this.recvPacket);

                pool.submit(() -> {
                    if (!Packet.checkSignature(data)) return;

                    if (data[2] == Packet.Type.CLIENT_AUDIO.id) {

                        if (this.connected.containsKey(clientAddress)) {
                            this.localClientAudioPacket.get().deserialize(data);
                            this.localServerAudioPacket.get().create(this.localClientAudioPacket.get(), this.connected.get(clientAddress).getUUID());

                            for (SocketAddress address : this.connected.keySet()) {
                                if (!Objects.equals(address, clientAddress)) {
                                    this.send(this.localServerAudioPacket.get().serialize(), address);
                                }
                            }
                        }

                    } else if (data[2] == Packet.Type.CLIENT_KEEP_ALIVE.id) {

                        if (this.connected.containsKey(clientAddress)) {
                            this.connected.get(clientAddress).setLastKeepAliveResponse(System.currentTimeMillis());
                        }

                    } else if (data[2] == Packet.Type.CLIENT_DISCONNECT.id) {

                        if (this.connected.containsKey(clientAddress)) {
                            ClientConnection disconnected = this.connected.remove(clientAddress);
                            BareBonesVCServer.LOGGER.info("Client disconnected: " + disconnected.getUsername() + " (" + disconnected.getUUID() + ")");
                        }

                    } else if (data[2] == Packet.Type.CLIENT_HELLO.id) {

                        this.localClientHelloPacket.get().deserialize(data);

                        if (this.connected.containsKey(clientAddress)) return;
                        BareBonesVCServer.LOGGER.info("Client connected: " + this.localClientHelloPacket.get().getUsername() + " (" + this.localClientHelloPacket.get().getUUID() + ")");

                        this.localServerHelloPacket.get().create(
                                this.config.mojangAuth,
                                this.config.mtuSize,
                                this.config.keepAliveInterval,
                                this.config.voiceDistance,
                                this.config.codec,
                                this.config.groupsEnabled
                        );
                        if (!this.send(this.localServerHelloPacket.get().serialize(), clientAddress)) {
                            BareBonesVCServer.LOGGER.severe("An error occurred while sending handshake to " + readableAddress);
                        }

                        this.connected.put(clientAddress, new ClientConnection(
                                this.localClientHelloPacket.get().getUsername(),
                                this.localClientHelloPacket.get().getUUID()
                        ));

                    }
                });
            }
            this.close();
        });

        networkThread.setDaemon(true);
        networkThread.setName("BareBonesNetworkThread");
        networkThread.start();


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
            this.send(this.serverDisconnectPacket.serialize(), address);
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
