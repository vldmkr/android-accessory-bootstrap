# Documentation

## `public abstract class AccessoryInterface`

This is an abstract class that implements the basic interaction with the USB device connected as USB host through Android Open Accessory (AOA) protocol.



Not all devices can support accessory mode. Suitable devices can be filtered using an <uses-feature> element in the AndroidManifest.



Communication with the code on the application level is carried out through the {@link Handler}. Posted item will be processed as soon as the message queue will be ready to do so. Although this does not guarantee high speed of {@link Message} processing, but it satisfies the requirements.



The methods {@link #create} and {@link #destroy} must be called from the appropriate antagonistic callbacks of the application's life cycle, such as {@link android.app.Activity#onResume} and {@link android.app.Activity#onPause}.



The methods {@link #getManufacturer}, {@link #getModel}, {@link #getVersion} are used to identify the USB accessory and must be implemented in the extended class.



The {@link #callback} method requires an override if the extended class does not use a communication {@link Handler}.

## `protected AccessoryInterface(final Handler communicationHandler, final int bufferSize)`

 * **Parameters:**
   * `communicationHandler` — {@link Handler} involved in the communication with application-level code.

     If it is null, messages are processed by the internal {@link Handler} and

     pushed to {@link #callback} as a parameter.
   * `bufferSize` — Expected data size of the {@link FileInputStream} from the USB device.

## `public final void create(final Context context)`

This method finds and opens an attached USB accessory if the caller has permission to access the accessory. Otherwise, the corresponding runtime permission request will be sent.

 * **Parameters:** `context` — {@link Context} for which the {@link BroadcastReceiver} will be registered.

## `public final void destroy(final Context context)`

This method closes the connection with USB accessory if it is attached.

 * **Parameters:** `context` — Context, which is accepted in the {@link #create} method.

## `protected void callback(Message msg)`

If communicationHandler param of {@link #AccessoryInterface} is null, messages are pushed to this callback.

 * **Parameters:** `msg` — {@link Message} received after processing by internal {@link Handler}.

## `public abstract String getManufacturer()`

 * **Returns:** The implementation must not return null, it is used to identify the USB accessory.

## `public abstract String getModel()`

 * **Returns:** The implementation must not return null, it is used to identify the USB accessory.

## `public abstract String getVersion()`

 * **Returns:** The implementation must not return null, it is used to identify the USB accessory.

## `protected final void write(byte[] data)`

Send data to the USB device. {@link FileChannel} from the non-blocking IO package is used.

 * **Parameters:** `data` — The data array to send.

## `protected final void directWrite(byte[] data, int byteOffset, int byteCount)`

Send data to the USB device. Blocking operation. {@link FileOutputStream} is used.

 * **Parameters:**
   * `data` — The data array to send.
   * `byteOffset` — The offset to the first byte of the data array to be send.
   * `byteCount` — The maximum number of bytes to send.