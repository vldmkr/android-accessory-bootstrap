package com.vldmkr;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.vldmkr.ft311d.AccessoryInterface;
import com.vldmkr.ft311d.FT311SPIMasterInterface;
import com.vldmkr.ft311d.bootstrap.R;

public class HX711Activity extends Activity {
    private final byte[] SPI_HIGH_LEVEL_PACKET = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
    private final byte[] SPI_LOW_LEVEL_PACKET = new byte[]{0, 0, 0, 0};
    private final int SPI_BYTES_TO_READ = 4;
    private final int SPI_READ_INTERVAL_MS = 200;

    private EditText mValueEditText = null;
    private EditText mTriggerEditText = null;
    private View mIconView = null;

    private final Handler mSPIMasterHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == FT311SPIMasterInterface.MSG_WHAT_SPI_WRITE_DATA
                    || msg.what == FT311SPIMasterInterface.MSG_WHAT_SPI_READ_DATA) {
                long val = (bytesToLong((byte[]) msg.obj) ^ 0x800000);
                if (val != 8388607) {
                    mValueEditText.setText(String.valueOf(val));

                    if (mContinuesRead && mReadHandler != null) {
                        mIconView.setVisibility(isTrigger() ? View.VISIBLE : View.INVISIBLE);
                        mReadHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mFT311SPIMasterInterface.writeDataSPI(
                                        isTrigger() ? SPI_HIGH_LEVEL_PACKET : SPI_LOW_LEVEL_PACKET);
                            }
                        }, SPI_READ_INTERVAL_MS);
                    }
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
    private boolean mContinuesRead = false;
    private Handler mReadHandler;

    public static long bytesToLong(byte[] b) {
        long result = 0;
        for (int i = 0; i < 3; i++) {
            result <<= 8;
            result |= (b[i] & 0xFF);
        }
        return result;
    }

    private boolean isTrigger() {
        if (mValueEditText != null && mTriggerEditText != null) {
            try {
                final int trigger = Integer.parseInt(mTriggerEditText.getText().toString());
                final int value = Integer.parseInt(mValueEditText.getText().toString());
                return value > trigger;
            } catch (NumberFormatException ignored) {
            }
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hx711);

        final HandlerThread readHandlerThread = new HandlerThread("HX711Activity.ReadHandlerThread");
        readHandlerThread.start();
        mReadHandler = new Handler(readHandlerThread.getLooper());

        mValueEditText = (EditText) findViewById(R.id.editTextValue);
        mTriggerEditText = (EditText) findViewById(R.id.editTextTrigger);
        mIconView = findViewById(R.id.imageViewIcon);

        findViewById(R.id.buttonRead).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFT311SPIMasterInterface.readDataSPI(SPI_BYTES_TO_READ, (byte) 0);
            }
        });

        findViewById(R.id.buttonTrigger).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mValueEditText != null && mTriggerEditText != null) {
                    mTriggerEditText.setText(mValueEditText.getText());
                }
            }
        });

        findViewById(R.id.buttonStart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContinuesRead = true;
                mFT311SPIMasterInterface.readDataSPI(SPI_BYTES_TO_READ, (byte) 0);
            }
        });

        findViewById(R.id.buttonStop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContinuesRead = false;
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
