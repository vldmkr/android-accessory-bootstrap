package com.vldmkr.ft311d;

import android.os.Handler;
import android.os.Message;

import java.util.Arrays;

public class FT311SPIMasterInterface extends AccessoryInterface {
    private static final String MANUFACTURER_STRING = "FTDI";
    private static final String MODEL_STRING = "FTDISPIMasterDemo";
    private static final String VERSION_STRING = "1.0";

    public static final byte CLOCK_PHASE_CPOL_0_CPHA_0 = 0;
    public static final byte CLOCK_PHASE_CPOL_0_CPHA_1 = 1;
    public static final byte CLOCK_PHASE_CPOL_1_CPHA_0 = 2;
    public static final byte CLOCK_PHASE_CPOL_1_CPHA_1 = 3;

    public static final byte DATA_ORDER_MSB = 0;
    public static final byte DATA_ORDER_LSB = 1;

    public static final int MSG_WHAT_SPI_READ_DATA = 11;
    public static final int MSG_WHAT_SPI_WRITE_DATA = 12;

    public final int MIN_CLOCK_FREQ = 150000;
    public final int MAX_CLOCK_FREQ = 24000000;
    public final int MAX_BYTES = 255;

    private final Handler mCommunicationHandler;

    public FT311SPIMasterInterface(final Handler communicationHandler) {
        super(null, 255);
        mCommunicationHandler = communicationHandler;
    }

    public void resetSPI() {
        write(new byte[]{0x64});
    }

    public void configSPI(byte clockPhase, byte dataOrder, int clockFreq) {
        clockFreq = clockFreq < MIN_CLOCK_FREQ ? MIN_CLOCK_FREQ : clockFreq;
        clockFreq = clockFreq > MAX_CLOCK_FREQ ? MAX_CLOCK_FREQ : clockFreq;
        write(new byte[]{
                0x61,
                clockPhase,
                dataOrder,
                (byte) (clockFreq & 0xff),
                (byte) ((clockFreq >> 8) & 0xff),
                (byte) ((clockFreq >> 16) & 0xff),
                (byte) ((clockFreq >> 24) & 0xff),
        });
    }

    public void readDataSPI(int count) {
        if (count > 0) {
            count = count > MAX_BYTES ? MAX_BYTES : count;
            final byte[] buffer = new byte[count + 1];
            Arrays.fill(buffer, (byte) 0xff);
            buffer[0] = 0x63;

            write(buffer);
        }
    }

    public void writeDataSPI(final byte[] data) {
        if (data != null && data.length > 0) {
            final int count = data.length > MAX_BYTES ? MAX_BYTES : data.length;
            final byte[] buffer = new byte[count + 1];
            System.arraycopy(data, 0, buffer, 1, count);
            buffer[0] = 0x62;

            write(buffer);
        }
    }

    @Override
    protected void callback(Message msg) {
        if (msg.what == MSG_WHAT_ACCESSORY_ROW_DATA) {
            final byte[] data = ((byte[]) msg.obj);
            final int actuallyRead = msg.arg1;
            if (data[0] == 0x63) {
                Message.obtain(mCommunicationHandler, MSG_WHAT_SPI_READ_DATA,
                        Arrays.copyOfRange(data, 1, actuallyRead)).sendToTarget();
            } else if (data[0] == 0x62) {
                Message.obtain(mCommunicationHandler, MSG_WHAT_SPI_WRITE_DATA,
                        Arrays.copyOfRange(data, 1, actuallyRead)).sendToTarget();
            }
        } else {
            Message newMsg = Message.obtain();
            newMsg.copyFrom(msg);
            newMsg.setTarget(mCommunicationHandler);
            newMsg.sendToTarget();
        }
    }

    @Override
    public String getManufacturer() {
        return MANUFACTURER_STRING;
    }

    @Override
    public String getModel() {
        return MODEL_STRING;
    }

    @Override
    public String getVersion() {
        return VERSION_STRING;
    }
}