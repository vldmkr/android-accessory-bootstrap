package com.vldmkr.accessories;

import android.os.Handler;
import android.os.Message;

import com.vldmkr.accessory.AccessoryInterface;

/**
 * The implementation of the {@link AccessoryInterface} to work with FT311 chip in GPIO mode.
 * [CNFG0 = 0, CNFG1 = 0, CNFG2 = 0] or [CNFG0 = 1, CNFG1 = 1, CNFG2 = 1]
 *
 * MANUFACTURER_STRING, MODEL_STRING, VERSION_STRING are the strings that determine the device.
 * These strings are configurable with a Windows utility FT311Configuration.exe
 * Current implementation contains their default values.
 */
public class FT311GPIOInterface extends AccessoryInterface {
    private static final String MANUFACTURER_STRING = "FTDI";
    private static final String MODEL_STRING = "FTDIGPIODemo";
    private static final String VERSION_STRING = "1.0";

    public static final int MSG_WHAT_GPIO_DATA = 0x13;

    private byte mPortState;
    private final Handler mCommunicationHandler;

    /**
     * The messages require additional processing and we will not post it upper in the raw format.
     * We will use internal {@link Handler} of the {@link AccessoryInterface} to process the raw messages.
     * To achieve this we need to override the {@link #callback(Message)} method.
     * The communication {@link Handler} of the superclass must be null.
     *
     * @param communicationHandler {@link Handler} involved in the communication with application-level code.
     */
    public FT311GPIOInterface(final Handler communicationHandler) {
        super(null, 4);
        mCommunicationHandler = communicationHandler;
    }

    public void resetPort() {
        write(new byte[]{0x14, 0x00, 0x00, 0x00});
    }

    public void configPort(byte configOutMap) {
        final byte configInMap = (byte) ~configOutMap;
        write(new byte[]{0x11, 0x00, ByteUtils.low(configOutMap, 7), ByteUtils.low(configInMap, 7)});
    }

    public void writePort(byte portData) {
        write(new byte[]{0x13, ByteUtils.low(portData, 7), 0x00, 0x00});
    }

    public byte readPort() {
        return mPortState;
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
}