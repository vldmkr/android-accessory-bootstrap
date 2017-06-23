[create]:               /app/src/main/java/com/vldmkr/accessories/AccessoryInterface.java#L129
[destroy]:              /app/src/main/java/com/vldmkr/accessories/AccessoryInterface.java#L177
[getManufacturer]:      /app/src/main/java/com/vldmkr/accessories/AccessoryInterface.java#L200
[getModel]:             /app/src/main/java/com/vldmkr/accessories/AccessoryInterface.java#L205
[getVersion]:           /app/src/main/java/com/vldmkr/accessories/AccessoryInterface.java#L210
[callback]:             /app/src/main/java/com/vldmkr/accessories/AccessoryInterface.java#L189
[AccessoryInterface]:   /app/src/main/java/com/vldmkr/accessories/AccessoryInterface.java#L109

[android.app.Activity#onResume]:      https://developer.android.com/reference/android/app/Activity.html#onResume()
[android.app.Activity#onPause]:       https://developer.android.com/reference/android/app/Activity.html#onPause()
[android.os.Handler]:                 https://developer.android.com/reference/android/os/Handler.html
[android.os.Message]:                 https://developer.android.com/reference/android/os/Message.html
[android.content.Context]:            https://developer.android.com/reference/android/content/Context.html
[android.content.BroadcastReceiver]:  https://developer.android.com/reference/android/content/BroadcastReceiver.html
[java.io.FileInputStream]:            https://developer.android.com/reference/java/io/FileInputStream.html
[java.nio.channels.FileChannel]:      https://developer.android.com/reference/java/nio/channels/FileChannel.html
[java.io.FileOutputStream]:           https://developer.android.com/reference/java/io/FileOutputStream.html

## Documentation

#### `public abstract class AccessoryInterface`

This is an abstract class that implements the basic interaction with the USB device connected as USB host through Android Open Accessory (AOA) protocol.



Not all devices can support accessory mode. Suitable devices can be filtered using an `<uses-feature>` element in the AndroidManifest.



Communication with the code on the application level is carried out through the [android.os.Handler]. Posted item will be processed as soon as the message queue will be ready to do so. Although this does not guarantee high speed of [android.os.Message] processing, but it satisfies the requirements.



The methods [create] and [destroy] must be called from the appropriate antagonistic callbacks of the application's life cycle, such as [android.app.Activity#onResume] and [android.app.Activity#onPause].



The methods [getManufacturer], [getModel], [getVersion] are used to identify the USB accessory and must be implemented in the extended class.



The [callback] method requires an override if the extended class does not use a communication [android.os.Handler].

#### `protected AccessoryInterface(final Handler communicationHandler, final int bufferSize)`

 * **Parameters:**
   * `communicationHandler` — [android.os.Handler] involved in the communication with application-level code.

     If it is null, messages are processed by the internal [android.os.Handler] and

     pushed to [callback] as a parameter.
   * `bufferSize` — Expected data size of the [java.io.FileInputStream] from the USB device.

#### `public final void create(final Context context)`

This method finds and opens an attached USB accessory if the caller has permission to access the accessory. Otherwise, the corresponding runtime permission request will be sent.

 * **Parameters:** `context` — [android.content.Context] for which the [android.content.BroadcastReceiver] will be registered.

#### `public final void destroy(final Context context)`

This method closes the connection with USB accessory if it is attached.

 * **Parameters:** `context` — Context, which is accepted in the [create] method.

#### `protected void callback(Message msg)`

If communicationHandler param of [AccessoryInterface] is null, messages are pushed to this callback.

 * **Parameters:** `msg` — [android.os.Message] received after processing by internal [android.os.Handler].

#### `public abstract String getManufacturer()`

 * **Returns:** The implementation must not return null, it is used to identify the USB accessory.

#### `public abstract String getModel()`

 * **Returns:** The implementation must not return null, it is used to identify the USB accessory.

#### `public abstract String getVersion()`

 * **Returns:** The implementation must not return null, it is used to identify the USB accessory.

#### `protected final void write(byte[] data)`

Send data to the USB device. [java.nio.channels.FileChannel] from the non-blocking IO package is used.

 * **Parameters:** `data` — The data array to send.

#### `protected final void directWrite(byte[] data, int byteOffset, int byteCount)`

Send data to the USB device. Blocking operation. [java.io.FileOutputStream] is used.

 * **Parameters:**
   * `data` — The data array to send.
   * `byteOffset` — The offset to the first byte of the data array to be send.
   * `byteCount` — The maximum number of bytes to send.
   
---   
The documentation is built using [Javadoc-to-Markdown](https://github.com/delight-im/Javadoc-to-Markdown)
