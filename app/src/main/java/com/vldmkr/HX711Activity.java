package com.vldmkr;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.vldmkr.ft311d.AccessoryInterface;
import com.vldmkr.ft311d.FT311SPIMasterInterface;
import com.vldmkr.ft311d.bootstrap.R;

public class HX711Activity extends Activity {
    private EditText mValueEditText = null;

    private final Handler mSPIMasterHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == FT311SPIMasterInterface.MSG_WHAT_SPI_WRITE_DATA) {
                long val = (bytesToLong((byte[]) msg.obj) ^ 0x800000);
                if (val != 8388607) {
                    mValueEditText.setText(String.valueOf(val));
                }
            } else if (msg.what < AccessoryInterface.MSG_WHAT_ACCESSORY_ROW_DATA) {
                final String detailed = msg.obj == null ? "" : msg.obj.toString();
                Toast.makeText(HX711Activity.this, "Accessory closed. " + detailed, Toast.LENGTH_LONG).show();
                finish();
            }
            return true;
        }
    });
    private final FT311SPIMasterInterface mFT311SPIMasterInterface = new FT311SPIMasterInterface(mSPIMasterHandler);

    public static long bytesToLong(byte[] b) {
        long result = 0;
        for (int i = 0; i < 3; i++) {
            result <<= 8;
            result |= (b[i] & 0xFF);
        }
        return result;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hx711);

        mValueEditText = (EditText) findViewById(R.id.editTextValue);

        findViewById(R.id.buttonRead).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFT311SPIMasterInterface.writeDataSPI(new byte[]{(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA});
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFT311SPIMasterInterface.create(getApplicationContext());
        mFT311SPIMasterInterface.resetSPI();
        mFT311SPIMasterInterface.configSPI(
                FT311SPIMasterInterface.CLOCK_PHASE_CPOL_0_CPHA_1,
                FT311SPIMasterInterface.DATA_ORDER_MSB,
                600000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFT311SPIMasterInterface.resetSPI();
        mFT311SPIMasterInterface.destroy(getApplicationContext());
    }
}
