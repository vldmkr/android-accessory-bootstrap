package com.vldmkr;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;

import com.vldmkr.FT311D.GPIO.AccessoryInterface;
import com.vldmkr.FT311D.GPIO.FT311GPIOInterface;
import com.vldmkr.ft311d.gpio.R;

public class MainActivity extends Activity {

    private EditText mReadPortEditText = null;
    private FT311GPIOInterface mFT311GPIOInterface = null;
    private final Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == FT311GPIOInterface.MSG_WHAT_GPIO_DATA && mReadPortEditText != null) {
                mReadPortEditText.setText(String.format("%7s", Integer.toBinaryString((Byte) msg.obj)).replace(' ', '0'));
            } else if (msg.what < AccessoryInterface.MSG_WHAT_ACCESSORY_ROW_DATA) {
                finish();
            }
            return true;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText configEdit = (EditText) findViewById(R.id.editTextConfig);
        final EditText writeEdit = (EditText) findViewById(R.id.editTextWrite);
        mReadPortEditText = (EditText) findViewById(R.id.editTextRead);

        findViewById(R.id.buttonConfig).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFT311GPIOInterface != null && configEdit != null) {
                    mFT311GPIOInterface.configPort(Byte.parseByte(configEdit.getText().toString(), 2));
                }
            }
        });

        findViewById(R.id.buttonWrite).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFT311GPIOInterface != null && writeEdit != null) {
                    mFT311GPIOInterface.writePort(Byte.parseByte(writeEdit.getText().toString(), 2));
                }
            }
        });

        findViewById(R.id.buttonReset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFT311GPIOInterface != null) {
                    mFT311GPIOInterface.resetPort();
                }
            }
        });

        mFT311GPIOInterface = new FT311GPIOInterface(getApplicationContext(), mHandler);
        mFT311GPIOInterface.resetPort();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mFT311GPIOInterface != null) {
            mFT311GPIOInterface.resume();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mFT311GPIOInterface != null) {
            mFT311GPIOInterface.destroy();
        }
    }
}
