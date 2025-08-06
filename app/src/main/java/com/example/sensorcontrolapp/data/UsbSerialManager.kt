package com.example.sensorcontrolapp.data

import android.app.PendingIntent
import android.content.*
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.util.Log
import com.hoho.android.usbserial.driver.*
import com.hoho.android.usbserial.util.SerialInputOutputManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.Executors

class UsbSerialManager(private val context: Context) {

    private val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
    private var serialPort: UsbSerialPort? = null
    private val executor = Executors.newSingleThreadExecutor()

    private val _receivedData = MutableStateFlow("")
    val receivedData = _receivedData.asStateFlow()

    private val bufferBuilder = StringBuilder()
    private val ACTION_USB_PERMISSION = "com.example.sensorcontrolapp.USB_PERMISSION"

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_USB_PERMISSION) {
                synchronized(this) {
                    val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        device?.let { openSerialPort(it) }
                    } else {
                        Log.e("UsbSerial", "Permission denied for device $device")
                    }
                }
            }
        }
    }

    init {
        val filter = IntentFilter(ACTION_USB_PERMISSION)
        context.registerReceiver(usbReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
    }

    fun findAndConnect() {
        val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager)
        if (availableDrivers.isEmpty()) {
            Log.e("UsbSerial", "No USB serial devices found")
            return
        }

        val driver = availableDrivers[0]
        val device = driver.device

        if (!usbManager.hasPermission(device)) {
            val permissionIntent = PendingIntent.getBroadcast(
                context, 0, Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE
            )
            usbManager.requestPermission(device, permissionIntent)
        } else {
            openSerialPort(device)
        }
    }

    private fun openSerialPort(device: UsbDevice) {
        val driver = UsbSerialProber.getDefaultProber().probeDevice(device) ?: return
        val connection = usbManager.openDevice(driver.device) ?: return

        serialPort = driver.ports[0]
        serialPort?.open(connection)
        serialPort?.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)

        val ioManager = SerialInputOutputManager(serialPort, object : SerialInputOutputManager.Listener {
            override fun onNewData(data: ByteArray) {
                val text = String(data)
                bufferBuilder.append(text)

                val lines = bufferBuilder.split("\n")
                for (i in 0 until lines.size - 1) {
                    val line = lines[i].trim()
                    if (line.isNotEmpty()) {
                        Log.d("UsbSerial", "Received line: $line")
                        _receivedData.value = line
                    }
                }

                // Keep the last partial line
                bufferBuilder.clear()
                bufferBuilder.append(lines.last())
            }

            override fun onRunError(e: Exception) {
                Log.e("UsbSerial", "Runner stopped.", e)
            }
        })

        executor.submit(ioManager)
        Log.i("UsbSerial", "Serial connection established")
    }

    fun send(data: String) {
        serialPort?.write(data.toByteArray(), 1000)
    }

    fun close() {
        serialPort?.close()
        executor.shutdown()
        Log.i("UsbSerial", "Serial connection closed")
    }
}
