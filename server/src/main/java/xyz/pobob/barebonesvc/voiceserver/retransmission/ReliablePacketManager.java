package xyz.pobob.barebonesvc.voiceserver.retransmission;

import xyz.pobob.barebonesvc.net.ReliablePacket;
import xyz.pobob.barebonesvc.net.ServerAckPacket;
import xyz.pobob.barebonesvc.util.Bytes;
import xyz.pobob.barebonesvc.voiceserver.ClientConnection;
import xyz.pobob.barebonesvc.voiceserver.VoiceServer;

import java.net.SocketAddress;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class ReliablePacketManager {

    private final VoiceServer server;

    public ReliablePacketManager(VoiceServer server) {
        this.server = server;
    }

    private static final long RETRANSMIT_TIMEOUT = 1000;
    private static final int MAX_RETRIES = 10;

    public void registerSequence(ReliablePacket packet, SocketAddress clientAddress) {
        ClientConnection conn = this.server.connected.get(clientAddress);
        if (conn != null) {
            packet.setSequenceNumber(conn.getAndIncrementNextSendSequence());
            conn.setPendingOutgoing(packet.getSequenceNumber(), new PendingPacket(packet, clientAddress, System.currentTimeMillis()));
        }
    }

    public void onClientAcknowledge(final int sequence, SocketAddress clientAddress) {
        ClientConnection conn = this.server.connected.get(clientAddress);
        if (conn != null) {
            conn.removePendingOutgoing(sequence);
        }
    }

    public void start() {
        this.server.scheduler.scheduleAtFixedRate(this::checkPendingPackets, 0L, 100L, TimeUnit.MILLISECONDS);
    }

    private void checkPendingPackets() {
        long now = System.currentTimeMillis();

        for (Map.Entry<SocketAddress, ClientConnection> client : this.server.connected.entrySet()) {
            Iterator<PendingPacket> iterator = client.getValue().getPendingOutgoingPackets().iterator();

            for (PendingPacket pending = iterator.next(); iterator.hasNext(); ) {

                if (now - pending.lastSent < RETRANSMIT_TIMEOUT) {
                    continue;
                }

                if (pending.retries >= MAX_RETRIES) {
                    iterator.remove();
                    this.server.onTimeout(client.getKey());
                    continue;
                }

                this.server.send(pending.packet, pending.address);

                pending.lastSent = now;
                pending.retries++;
            }
        }
    }

    public void receive(byte[] data, SocketAddress clientAddress) {
        ClientConnection conn = this.server.connected.get(clientAddress);

        int sequence = Bytes.getInt(data, ReliablePacket.SEQUENCE_INDEX);

        if (sequence < conn.getExpectedReceiveSequence()) {
            this.sendAck(sequence, clientAddress);
            return;
        }

        if (sequence > conn.getExpectedReceiveSequence()) {
            conn.setQueuedReceived(sequence, data);
            return;
        }

        this.processSequential(data, clientAddress, conn);
    }

    private void processSequential(byte[] data, SocketAddress clientAddress, ClientConnection connection) {

        byte[] current = data;

        while (current != null) {

            this.server.clientMessageDispatcher.dispatch(data, clientAddress);

            this.sendAck(Bytes.getInt(data, ReliablePacket.SEQUENCE_INDEX), clientAddress);

            current = connection.getAndRemoveQueuedReceived(connection.getAndIncrementExpectedReceiveSequence());
        }
    }

    private final ThreadLocal<ServerAckPacket> localServerAckPacket = ThreadLocal.withInitial(ServerAckPacket::new);

    private void sendAck(int sequence, SocketAddress clientAddress) {
        this.localServerAckPacket.get().create(sequence);
        this.server.send(this.localServerAckPacket.get(), clientAddress);
    }
}
