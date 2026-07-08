package xyz.pobob.barebonesvc.packet.retransmission;

import xyz.pobob.barebonesvc.packet.ReliablePacket;
import xyz.pobob.barebonesvc.packet.ServerAckPacket;
import xyz.pobob.barebonesvc.packet.registry.PacketRegistry;
import xyz.pobob.barebonesvc.util.Bytes;
import xyz.pobob.barebonesvc.voiceserver.BareBonesVCServer;
import xyz.pobob.barebonesvc.voiceserver.ClientConnection;

import java.net.SocketAddress;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class ReliablePacketManager {

    private final BareBonesVCServer server;

    public ReliablePacketManager(BareBonesVCServer server) {
        this.server = server;
    }

    private static final long RETRANSMIT_TIMEOUT = 1000;
    private static final int MAX_RETRIES = 15;

    public void registerSequence(ReliablePacket packet, SocketAddress clientAddress) {
        ClientConnection conn = this.server.getClient(clientAddress);
        if (conn != null) {
            packet.setSequenceNumber(conn.getAndIncrementNextSendSequence());
            conn.setSentPendingPacket(packet.getSequenceNumber(), new PendingPacket(packet.serialize(), clientAddress, System.currentTimeMillis()));
        }
    }

    public void onClientAcknowledge(final int sequence, SocketAddress clientAddress) {
        ClientConnection conn = this.server.getClient(clientAddress);
        if (conn != null) {
            conn.removeSentPendingPacket(sequence);
        }
    }

    public void startCheckingPendingPackets() {
        this.server.scheduler.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();

            for (Map.Entry<SocketAddress, ClientConnection> client : this.server.getAuthenticatedEntries()) {
                Iterator<PendingPacket> iterator = client.getValue().getSentPendingPackets().iterator();

                while (iterator.hasNext()) {
                    PendingPacket pending = iterator.next();

                    if (now - pending.lastSent < RETRANSMIT_TIMEOUT) {
                        continue;
                    }

                    if (pending.retries >= MAX_RETRIES) {
                        iterator.remove();
                        this.server.onTimeout(client.getKey());
                        continue;
                    }

                    this.server.send(pending.data, pending.address);

                    pending.lastSent = now;
                    pending.retries++;
                }
            }
        }, 0L, 100L, TimeUnit.MILLISECONDS);
    }

    public void receive(byte[] data, SocketAddress clientAddress) {
        ClientConnection conn = this.server.getClient(clientAddress);

        if (conn != null) {
            int sequence = Bytes.getInt(data, ReliablePacket.SEQUENCE_INDEX);
            this.sendAck(sequence, clientAddress);

            if (sequence < conn.getExpectedReceiveSequence()) return;

            if (sequence > conn.getExpectedReceiveSequence()) {
                conn.addToReceivedQueue(sequence, data);
                return;
            }

            this.processSequential(data, clientAddress, conn);
        }
    }

    private void processSequential(byte[] data, SocketAddress clientAddress, ClientConnection connection) {
        byte[] current = data;

        while (current != null) {

            PacketRegistry.dispatchClientPacket(data, clientAddress);

            current = connection.pollReceivedQueue(connection.getAndIncrementExpectedReceiveSequence());
        }
    }

    private final ThreadLocal<ServerAckPacket> localServerAckPacket = ThreadLocal.withInitial(ServerAckPacket::new);

    private void sendAck(int sequence, SocketAddress clientAddress) {
        this.localServerAckPacket.get().create(sequence);
        this.server.send(this.localServerAckPacket.get(), clientAddress);
    }
}
