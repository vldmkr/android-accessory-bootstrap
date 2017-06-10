package com.vldmkr.accessories;

public class ByteUtils {

    public static byte highMask(byte value, int mask) {
        return (byte) (value | mask);
    }

    public static byte lowMask(byte value, int mask) {
        return (byte) (value & ~mask);
    }

    public static byte high(byte value, int bit) {
        return highMask(value, 1 << bit);
    }

    public static byte low(byte value, int bit) {
        return lowMask(value, 1 << bit);
    }

    public static byte bit(byte value, int bit) {
        return (byte) ((value >> bit) & 1);
    }

    public static long shiftInMsb(byte[] bytes, int size) {
        final int BYTES_IN_LONG = Long.SIZE / Byte.SIZE;
        size = size > BYTES_IN_LONG ? BYTES_IN_LONG : size;
        long value = 0;
        for (int i = 0; i < size; i++) {
            value = (value << Byte.SIZE) | (bytes[i] & 0xff);
        }
        return value;
    }

    public static long shiftInLsb(byte[] bytes, int size) {
        final int BYTES_IN_LONG = Long.SIZE / Byte.SIZE;
        size = size > BYTES_IN_LONG ? BYTES_IN_LONG : size;
        long value = 0;
        for (int i = 0; i < size; i++) {
            value |= (bytes[i] & 0xff) << (Byte.SIZE * i);
        }
        return value;
    }
}
