package com.vldmkr.accessories;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Locale;

/**
 * This is an abstract class that implements the basic interaction with
 * the USB device connected as USB host through Android Open Accessory (AOA) protocol.
 *
 * <p>Not all devices can support accessory mode. Suitable devices can be filtered
 * using a <uses-feature> element in the AndroidManifest.
 *
 * <p>Communication with the application level code is carried out through the {@link Handler}.
 * Posted item will be processed as soon as the message queue will ready to do so.
 * Although this does not guarantee high speed of {@link Message} processing,
 * but it satisfies the requirements.
 *
 * <p>The methods {@link #create} and {@link #destroy} must be called from the antagonistic callbacks
 * of the application's life cycle, such as {@link Activity#onResume} and {@link Activity#onPause},
 * respectively.
 *
 * <p>The methods
 * {@link #getManufacturer},
 * {@link #getModel},
 * {@link #getVersion}
 * are used to identify the USB accessory and must be implemented in the extended class.
 *
 * <p>The {@link #callback} method requires an override if the extended class does not use
 * a communication {@link Handler}.
 */
public abstract class AccessoryInterface {
    private static final String TAG = AccessoryInterface.class.getSimpleName();

    public static final int MSG_WHAT_ACCESSORY_ROW_DATA = -1;               // obj is byte[]
    public static final int MSG_WHAT_ACCESSORY_NOT_CONNECTED = -2;          // obj is null
    public static final int MSG_WHAT_ACCESSORY_DETACHED = -3;               // obj is null
    public static final int MSG_WHAT_ACCESSORY_PERMISSION_NOT_GRANTED = -4; // obj is null
    public static final int MSG_WHAT_ACCESSORY_INEQUALITY = -5;             // obj is String

    private static final String ACTION_USB_PERMISSION = "com.AccessoryInterface.USB_PERMISSION";
    private boolean mIsPermissionRequestPending = false;
    private PendingIntent mPermissionIntent = null;

    private UsbManager mUsbManager = null;

    private ParcelFileDescriptor mFileDescriptor = null;
    private FileInputStream mInputStream = null;
    private FileOutputStream mOutputStream = null;
    private FileChannel mInChanel = null;
    private FileChannel mOutChanel = null;

    private final ByteBuffer mInBuffer;

    private final Handler mWorkerHandler;
    private final Handler mCommunicationHandler;

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                UsbAccessory accessory = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    open(accessory);
                } else {
                    Message.obtain(mCommunicationHandler, MSG_WHAT_ACCESSORY_PERMISSION_NOT_GRANTED).sendToTarget();
                }
                mIsPermissionRequestPending = false;
            } else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
                close();
                Message.obtain(mCommunicationHandler, MSG_WHAT_ACCESSORY_DETACHED).sendToTarget();
            }
        }
    };

    /**
     *
     * @param communicationHandler
     * @param bufferSize
     */
    public AccessoryInterface(final Handler communicationHandler, final int bufferSize) {
        final HandlerThread workerHandlerThread = new HandlerThread("AccessoryInterface.WorkerHandlerThread");
        workerHandlerThread.start();
        mWorkerHandler = new Handler(workerHandlerThread.getLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                callback(msg);
                return true;
            }
        });
        mCommunicationHandler = communicationHandler != null ? communicationHandler : mWorkerHandler;
        mInBuffer = ByteBuffer.allocate(bufferSize);
    }

    /**
     * This method finds and opens an attached USB accessory if the caller has permission
     * to access the accessory. If not, the corresponding request will be sent.
     *
     * @param context Context for which the {@link BroadcastReceiver} will be registered.
     */
    public final void create(final Context context) {
        mUsbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
        context.registerReceiver(mUsbReceiver, new IntentFilter() {{
            addAction(ACTION_USB_PERMISSION);
            addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        }});

        start();
    }

    /**
     * This method closes the connection with USB accessory if it is attached.
     *
     * @param context Context, which is accepted in the {@link #create} method.
     */
    public final void destroy(final Context context) {
        context.unregisterReceiver(mUsbReceiver);

        close();
    }

    protected void callback(Message msg) {
        String logMsg = String.format(Locale.ENGLISH, "default callback; override it; message code: %d", msg.what);
        if (msg.what == MSG_WHAT_ACCESSORY_ROW_DATA) {
            logMsg = String.format(Locale.ENGLISH, "%s; row data: %s", logMsg, Arrays.toString((byte[]) msg.obj));
        }
        Log.w(TAG, logMsg);
    }

    public abstract String getManufacturer();

    public abstract String getModel();

    public abstract String getVersion();

    private void start() {
        if (mFileDescriptor == null) {
            final UsbAccessory[] accessories = mUsbManager.getAccessoryList();
            // in the current implementation of UsbManager there can be at most one attached USB accessory
            if (accessories != null && accessories[0] != null) {
                final UsbAccessory accessory = accessories[0];
                if (!getManufacturer().equals(accessory.getManufacturer())) {
                    Message.obtain(mCommunicationHandler, MSG_WHAT_ACCESSORY_INEQUALITY,
                            "Manufacturer is not matched!").sendToTarget();
                    return;
                }
                if (!getModel().equals(accessory.getModel())) {
                    Message.obtain(mCommunicationHandler, MSG_WHAT_ACCESSORY_INEQUALITY,
                            "Model is not matched!").sendToTarget();
                    return;
                }
                if (!getVersion().equals(accessory.getVersion())) {
                    Message.obtain(mCommunicationHandler, MSG_WHAT_ACCESSORY_INEQUALITY,
                            "Version is not matched!").sendToTarget();
                    return;
                }

                if (mUsbManager.hasPermission(accessory)) {
                    open(accessory);
                } else {
                    if (!mIsPermissionRequestPending) {
                        mUsbManager.requestPermission(accessory, mPermissionIntent);
                        mIsPermissionRequestPending = true;
                    }
                }
            } else {
                Message.obtain(mCommunicationHandler, MSG_WHAT_ACCESSORY_NOT_CONNECTED).sendToTarget();
            }
        }
    }

    private void open(final UsbAccessory accessory) {
        mFileDescriptor = mUsbManager.openAccessory(accessory);
        if (mFileDescriptor != null) {
            final FileDescriptor fd = mFileDescriptor.getFileDescriptor();
            mInputStream = new FileInputStream(fd);
            mInChanel = mInputStream.getChannel();
            mOutputStream = new FileOutputStream(fd);
            mOutChanel = mOutputStream.getChannel();

            mWorkerHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (mInChanel != null) {
                            final int actuallyRead = mInChanel.read(mInBuffer);
                            Message.obtain(mCommunicationHandler, MSG_WHAT_ACCESSORY_ROW_DATA,
                                    actuallyRead, 0, mInBuffer.array()).sendToTarget();
                            mInBuffer.clear();
                            mWorkerHandler.post(this);
                        }
                    } catch (IOException ignored) {
                        mWorkerHandler.removeCallbacks(this);
                    }
                }
            });
        }
    }

    protected final void write(byte[] data) {
        try {
            if (mOutChanel != null) {
                mOutChanel.write(ByteBuffer.wrap(data));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected final void directWrite(byte[] data, int byteOffset, int byteCount) {
        try {
            if (mOutputStream != null) {
                mOutputStream.write(data, byteOffset, byteCount);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void close() {
        mWorkerHandler.removeCallbacksAndMessages(null);

        try {
            if (mFileDescriptor != null) {
                mFileDescriptor.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            mFileDescriptor = null;
        }

        try {
            if (mInputStream != null) {
                mInputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            mInputStream = null;
        }

        try {
            if (mInChanel != null) {
                mInChanel.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            mInChanel = null;
        }

        try {
            if (mOutputStream != null) {
                mOutputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            mOutputStream = null;
        }

        try {
            if (mOutChanel != null) {
                mOutChanel.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            mOutChanel = null;
        }
    }
}
