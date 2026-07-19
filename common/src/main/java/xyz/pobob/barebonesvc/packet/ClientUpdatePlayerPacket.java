package xyz.pobob.barebonesvc.packet;

import xyz.pobob.barebonesvc.packet.retransmission.ReliablePacket;
import xyz.pobob.barebonesvc.util.Bytes;

/**
 * [DISABLED + DISCONNECTED : 1]
 */
public class ClientUpdatePlayerPacket extends ReliablePacket {

    private boolean disabled;
    private boolean disconnected;

    public boolean isDisabled() {
        return this.disabled;
    }

    public boolean isDisconnected() {
        return this.disconnected;
    }

    public void create(boolean disabled, boolean disconnected) {
        this.disabled = disabled;
        this.disconnected = disconnected;
    }

    @Override
    public byte[] serialize() {
        return Bytes.join(
                this.createHeader(1),
                new byte[] {(byte) ((this.disabled ? 1 : 0) + (this.disconnected ? 2 : 0))}
        );
    }

    @Override
    public void deserialize(byte[] data) {
        int start = this.getPayloadStart();

        this.disabled = (data[start] & 1) == 1;
        this.disconnected = (data[start] & 2) == 2;
    }
}
