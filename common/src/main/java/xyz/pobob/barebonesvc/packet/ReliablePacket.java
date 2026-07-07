package xyz.pobob.barebonesvc.packet;

/**
 * [MAGIC : 1][VERSION : 1][TYPE : 1][LENGTH : 2][SEQUENCE : 4][PAYLOAD]
 */
public abstract class ReliablePacket implements Packet {
    public static final int SEQUENCE_INDEX = 5;
    public static final int PAYLOAD_INDEX = 9;

    protected int sequenceNumber;

    public void setSequenceNumber(int value) {
        this.sequenceNumber = value;
    }

    public int getSequenceNumber() {
        return this.sequenceNumber;
    }
}
