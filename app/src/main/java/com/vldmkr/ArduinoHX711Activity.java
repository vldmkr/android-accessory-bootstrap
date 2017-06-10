package com.vldmkr;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.vldmkr.accessories.AccessoryInterface;
import com.vldmkr.accessories.ArduinoHx711Interface;
import com.vldmkr.accessories.ByteUtils;
import com.vldmkr.accessories.bootstrap.R;

public class ArduinoHX711Activity extends Activity {
    private final Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == AccessoryInterface.MSG_WHAT_ACCESSORY_ROW_DATA) {
                final long HX711Value = ByteUtils.shiftInLsb((byte[]) msg.obj, 3);
                mValueEditText.setText(String.valueOf(HX711Value));

                final boolean triggered = isTriggered();
                mIconView.setVisibility(triggered ? View.VISIBLE : View.INVISIBLE);
                mArduinoHx711Interface.ledPower(triggered);
            } else if (msg.what < AccessoryInterface.MSG_WHAT_ACCESSORY_ROW_DATA) {
                final String detailed = msg.obj == null ? "" : msg.obj.toString();
                Toast.makeText(ArduinoHX711Activity.this, "Accessory closed. " + detailed, Toast.LENGTH_LONG).show();
                finish();
            }
            return true;
        }
    });

    private final ArduinoHx711Interface mArduinoHx711Interface = new ArduinoHx711Interface(mHandler);

    private EditText mValueEditText = null;
    private EditText mTriggerEditText = null;
    private View mIconView = null;

    private boolean isTriggered() {
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
        setContentView(R.layout.activity_arduino);

        mValueEditText = (EditText) findViewById(R.id.editTextValue);
        mTriggerEditText = (EditText) findViewById(R.id.editTextTrigger);
        mIconView = findViewById(R.id.imageLed);

        findViewById(R.id.buttonTrigger).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mValueEditText != null && mTriggerEditText != null) {
                    mTriggerEditText.setText(mValueEditText.getText());
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mArduinoHx711Interface.create(getApplicationContext());
    }

    @Override
    protected void onPause() {
        super.onPause();
        mArduinoHx711Interface.destroy(getApplicationContext());
    }
}
