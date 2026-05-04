package xyz.pobob.barebonesvc.voiceserver;

import xyz.pobob.barebonesvc.BareBonesVCServer;
import xyz.pobob.barebonesvc.cli.command.CommandDispatcher;
import xyz.pobob.barebonesvc.cli.command.ConsoleListener;
import xyz.pobob.barebonesvc.cli.command.ListCommand;
import xyz.pobob.barebonesvc.cli.command.StopCommand;
import xyz.pobob.barebonesvc.net.*;
import xyz.pobob.barebonesvc.voiceserver.thread.ServerKeepAliveThreads;

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
    private final ThreadLocal<ClientKeepAlivePacket> localClientKeepAlivePacket = ThreadLocal.withInitial(ClientKeepAlivePacket::new);
    private final ThreadLocal<ServerKeepAlivePacket> localServerKeepAlivePacket = ThreadLocal.withInitial(ServerKeepAlivePacket::new);
    private final ThreadLocal<ClientUpdatePlayerPacket> localClientUpdatePlayerPacket = ThreadLocal.withInitial(ClientUpdatePlayerPacket::new);
    private final ThreadLocal<ServerUpdatePlayerPacket> localServerUpdatePlayerPacket = ThreadLocal.withInitial(ServerUpdatePlayerPacket::new);

    private final ServerClosePacket serverClosePacket = new ServerClosePacket();

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
        console.setDaemon(false);
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

                            this.announce(clientAddress, this.localServerAudioPacket.get().serialize());
                        }

                    } else if (data[2] == Packet.Type.CLIENT_KEEP_ALIVE.id) {

                        if (this.connected.containsKey(clientAddress)) {
                            this.connected.get(clientAddress).setLastKeepAliveResponse(System.currentTimeMillis());

                            this.localClientKeepAlivePacket.get().deserialize(data);
                            this.localServerKeepAlivePacket.get().create(this.localClientKeepAlivePacket.get().getId());
                            this.send(this.localServerKeepAlivePacket.get().serialize(), clientAddress);
                        }

                    } else if (data[2] == Packet.Type.CLIENT_UPDATE_PLAYER.id) {

                        if (this.connected.containsKey(clientAddress)) {
                            ClientUpdatePlayerPacket clientUpdatePlayerPacket = this.localClientUpdatePlayerPacket.get();
                            clientUpdatePlayerPacket.deserialize(data);

                            if (clientUpdatePlayerPacket.isDisconnected()) {
                                ClientConnection disconnected = this.connected.remove(clientAddress);
                                BareBonesVCServer.LOGGER.info("Client disconnected: " + disconnected.getUsername() + " (" + disconnected.getUUID() + ")");

                                this.localServerUpdatePlayerPacket.get().create(disconnected.getUsername(), disconnected.getUUID(), disconnected.isDisabled(), true);
                            } else {
                                ClientConnection connection = this.connected.get(clientAddress);
                                connection.setDisabled(clientUpdatePlayerPacket.isDisabled());

                                this.localServerUpdatePlayerPacket.get().create(connection.getUsername(), connection.getUUID(), clientUpdatePlayerPacket.isDisabled(), false);
                            }

                            this.announce(clientAddress, this.localServerUpdatePlayerPacket.get().serialize());
                        }

                    } else if (data[2] == Packet.Type.CLIENT_HELLO.id) {

                        ClientHelloPacket clientHelloPacket = this.localClientHelloPacket.get();

                        clientHelloPacket.deserialize(data);

                        if (this.connected.containsKey(clientAddress)) return;
                        BareBonesVCServer.LOGGER.info("Client connected: " + clientHelloPacket.getUsername() + " (" + clientHelloPacket.getUUID() + ")");

                        this.localServerHelloPacket.get().create(
                                this.config.mojangAuth,
                                this.config.voiceDistance,
                                this.config.codec,
                                this.config.groupsEnabled
                        );
                        if (!this.send(this.localServerHelloPacket.get().serialize(), clientAddress)) {
                            BareBonesVCServer.LOGGER.severe("An error occurred while sending handshake to " + readableAddress);
                            return;
                        }

                        this.connected.put(clientAddress, new ClientConnection(
                                clientHelloPacket.getUsername(),
                                clientHelloPacket.getUUID(),
                                clientHelloPacket.isDisabled()
                        ));

                        ServerUpdatePlayerPacket serverUpdatePlayerPacket = this.localServerUpdatePlayerPacket.get();
                        for (ClientConnection client : this.connected.values()) {
                            serverUpdatePlayerPacket.create(client.getUsername(), client.getUUID(), client.isDisabled(), false);
                            this.send(serverUpdatePlayerPacket.serialize(), clientAddress);
                        }

                        serverUpdatePlayerPacket.create(clientHelloPacket.getUsername(), clientHelloPacket.getUUID(), clientHelloPacket.isDisabled(), false);
                        this.announce(clientAddress, serverUpdatePlayerPacket.serialize());

                    }
                });
            }
            this.close();
        });

        networkThread.setDaemon(true);
        networkThread.setName("BareBonesNetworkThread");
        networkThread.start();

    }

    private void announce(SocketAddress src, byte[] data) {
        for (SocketAddress address : this.connected.keySet()) {
            if (!Objects.equals(address, src)) {
                this.send(data, address);
            }
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
