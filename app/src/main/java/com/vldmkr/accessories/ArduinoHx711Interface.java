package com.vldmkr.accessories;

import android.os.Handler;

public class ArduinoHx711Interface extends AccessoryInterface {
    private static final String MANUFACTURER_STRING = "Arduino";
    private static final String MODEL_STRING = "HX711";
    private static final String VERSION_STRING = "1.0";
    
    public ArduinoHx711Interface(Handler communicationHandler) {
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
