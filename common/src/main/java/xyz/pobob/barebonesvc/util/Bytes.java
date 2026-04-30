package xyz.pobob.barebonesvc.util;

import java.nio.charset.StandardCharsets;

public final class Bytes {
    public static byte[] join(byte[]... bytesList) {
        int size = 0;

        for (byte[] bytes : bytesList) {
            size += bytes.length;
        }

        byte[] data = new byte[size];

        int offset = 0;
        for (byte[] bytes : bytesList) {
            System.arraycopy(bytes, 0, data, offset, bytes.length);
            offset += bytes.length;
        }

        return data;
    }

    public static byte[] of(String string) {
        return string.getBytes(StandardCharsets.UTF_8);
    }

    public static String getString(byte[] data, int startIndex, int len) {
        byte[] bytes = new byte[len];
        System.arraycopy(data, startIndex, bytes, 0, len);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static byte[] of(short s) {
        return new byte[] {
                (byte) ((s >> 8) & 0xff),
                (byte) (s & 0xff)
        };
    }

    public static short getShort(byte[] data, int startIndex) {
        return (short) (((data[startIndex] & 0xff) << 8)
                | (data[startIndex+1] & 0xff));
    }

    public static byte[] of(int i) {
        return new byte[] {
                (byte) ((i >> 24) & 0xff),
                (byte) ((i >> 16) & 0xff),
                (byte) ((i >> 8) & 0xff),
                (byte) (i & 0xff)
        };
    }

    public static int getInt(byte[] data, int startIndex) {
        return ((data[startIndex] & 0xff) << 24)
                | ((data[startIndex+1] & 0xff) << 16)
                | ((data[startIndex+2] & 0xff) << 8)
                | (data[startIndex+3] & 0xff);
    }

    public static byte[] of(long l) {
        return new byte[] {
                (byte) ((l >> 56) & 0xff),
                (byte) ((l >> 48) & 0xff),
                (byte) ((l >> 40) & 0xff),
                (byte) ((l >> 32) & 0xff),
                (byte) ((l >> 24) & 0xff),
                (byte) ((l >> 16) & 0xff),
                (byte) ((l >> 8) & 0xff),
                (byte) (l & 0xff)
        };
    }

    public static long getLong(byte[] data, int startIndex) {
        return ((long) (data[startIndex] & 0xff) << 56)
                | ((long) (data[startIndex+1] & 0xff) << 48)
                | ((long) (data[startIndex+2] & 0xff) << 40)
                | ((long) (data[startIndex+3] & 0xff) << 32)
                | ((long) (data[startIndex+4] & 0xff) << 24)
                | ((data[startIndex+5] & 0xff) << 16)
                | ((data[startIndex+6] & 0xff) << 8)
                | (data[startIndex+7] & 0xff);
    }
}
