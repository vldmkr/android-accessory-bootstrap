package com.vldmkr.FT311D.GPIO;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

public class FT311GPIOInterface extends AccessoryInterface {
    private static final String MANUFACTURER_STRING = "FTDI";
    private static final String MODEL_STRING = "FTDIGPIODemo";
    private static final String VERSION_STRING = "1.0";

    public static final int MSG_WHAT_GPIO_DATA = 1;

    private byte mPortState;
    private final Context mContext;
    private final Handler mCommunicationHandler;

    public FT311GPIOInterface(final Context context, final Handler communicationHandler) {
        super(null, 4);
        mContext = context;
        mCommunicationHandler = communicationHandler;
        register(mContext);
    }

    public void resetPort() {
        write(new byte[]{0x14, 0x00, 0x00, 0x00});
    }

    public void configPort(byte configOutMap) {
        final byte configInMap = (byte) ~configOutMap;
        write(new byte[]{0x11, 0x00, low(configOutMap, 7), low(configInMap, 7)});
    }

    public void writePort(byte portData) {
        write(new byte[]{0x13, low(portData, 7), 0x00, 0x00});
    }

    public byte readPort() {
        return mPortState;
    }

    public void destroy() {
        resetPort();
        unregister(mContext);
    }

    @Override
    protected void callback(Message msg) {
        if (msg.what == MSG_WHAT_ACCESSORY_ROW_DATA) {
            mPortState = ((byte[]) msg.obj)[1];
            Message.obtain(mCommunicationHandler, MSG_WHAT_GPIO_DATA, mPortState).sendToTarget();
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

    static byte highMask(byte value, int mask) {
        return (byte) (value | mask);
    }

    static byte lowMask(byte value, int mask) {
        return (byte) (value & ~mask);
    }

    static byte high(byte value, int bit) {
        return highMask(value, 1 << bit);
    }

    static byte low(byte value, int bit) {
        return lowMask(value, 1 << bit);
    }
}