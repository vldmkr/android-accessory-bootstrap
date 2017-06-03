package com.vldmkr;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.vldmkr.accessories.AccessoryInterface;
import com.vldmkr.accessories.FT311GPIOInterface;
import com.vldmkr.accessories.bootstrap.R;

public class GPIOActivity extends Activity {
    private EditText mReadPortEditText = null;

    private final Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == FT311GPIOInterface.MSG_WHAT_GPIO_DATA && mReadPortEditText != null) {
                mReadPortEditText.setText(String.format("%7s", Integer.toBinaryString((Byte) msg.obj)).replace(' ', '0'));
            } else if (msg.what < AccessoryInterface.MSG_WHAT_ACCESSORY_ROW_DATA) {
                final String detailed = msg.obj == null ? "" : msg.obj.toString();
                Toast.makeText(GPIOActivity.this, "Accessory closed. " + detailed, Toast.LENGTH_LONG).show();
                finish();
            }
            return true;
        }
    });
    private final FT311GPIOInterface mFT311GPIOInterface = new FT311GPIOInterface(mHandler);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpio);

        final EditText configEdit = (EditText) findViewById(R.id.editTextConfig);
        final EditText writeEdit = (EditText) findViewById(R.id.editTextWrite);
        mReadPortEditText = (EditText) findViewById(R.id.editTextRead);

        findViewById(R.id.buttonConfig).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (configEdit != null) {
                    mFT311GPIOInterface.configPort(Byte.parseByte(configEdit.getText().toString(), 2));
                }
            }
        });

        findViewById(R.id.buttonWrite).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (writeEdit != null) {
                    mFT311GPIOInterface.writePort(Byte.parseByte(writeEdit.getText().toString(), 2));
                }
            }
        });

        findViewById(R.id.buttonReset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFT311GPIOInterface.resetPort();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFT311GPIOInterface.create(getApplicationContext());
        mFT311GPIOInterface.resetPort();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFT311GPIOInterface.resetPort();
        mFT311GPIOInterface.destroy(getApplicationContext());
    }
}
