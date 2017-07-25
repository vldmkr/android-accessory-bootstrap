package com.vldmkr.accessories;

import android.os.Handler;
import android.os.Message;

import com.vldmkr.accessory.AccessoryInterface;

/**
 * The minimal implementation of the {@link AccessoryInterface} for work with Arduino board equipped with the HX711 sensor.
 *
 * Arduino sketch and instructions you can find here:
 * https://github.com/vldmkr/arduino-adk-hx711
 */
public class ArduinoHx711Interface extends AccessoryInterface {
    private static final String MANUFACTURER_STRING = "Arduino";
    private static final String MODEL_STRING = "HX711";
    private static final String VERSION_STRING = "1.0";

    /**
     * Messages do not require additional processing and are sent directly from
     * {@link AccessoryInterface} to the application.
     * In this case, we do not need to override {@link #callback(Message)} since it is used only
     * if the communication {@link Handler} of the superclass are null.
     *
     * @param communicationHandler {@link Handler} involved in the communication with application-level code.
     */
    public ArduinoHx711Interface(final Handler communicationHandler) {
        super(communicationHandler, 4);
    }

    public void ledPower(boolean status) {
        write(new byte[]{(byte) (status ? 0x01 : 0x00)});
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
