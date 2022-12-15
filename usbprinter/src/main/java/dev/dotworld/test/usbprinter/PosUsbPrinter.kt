package dev.dotworld.test.usbprinter

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.util.Log
import androidx.core.content.getSystemService
import com.lvrenyang.io.Pos
import com.lvrenyang.io.USBPrinting
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object PosUsbPrinter {

    //companion object {
    private val TAG: String = "PosUsbPrinter"
    private val executorService: ExecutorService = Executors.newScheduledThreadPool(30)
    private var pos = Pos()
    private val usbPrinting = USBPrinting()
    private lateinit var mUsbManager: UsbManager
    private var mDevice: UsbDevice? = null
    private lateinit var mDeviceList: HashMap<String, UsbDevice>
    private var devices: ArrayList<UsbDevice> = arrayListOf()
    // }

    fun posSetup(context: Context) {
        pos.Set(usbPrinting)
        usbPrinting.SetCallBack(context.getSystemService())
        mUsbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        mDeviceList = mUsbManager.deviceList;
    }

    fun usbDevice(): UsbDevice? {
        return mDevice
    }

    fun setUsbDevice(de: UsbDevice) {
        mDevice = de
    }

    fun searchAndSelectUsbPost(context: Context): UsbDevice? {
        Log.d(TAG, "searchAndSelectUsbPost: start")
        var device: UsbDevice? = null
        val deviceName: List<String> = mDeviceList.keys.map { it }
        var d: Array<String?> = arrayOfNulls(deviceName.size)
        val deviceIterator: Iterator<UsbDevice> = mDeviceList.values.iterator()
        while (deviceIterator.hasNext()) {
            devices.add(deviceIterator.next())
        }

        Log.d(TAG, "searchAndSelectUsbPost: devices = ${devices.size}")
        for (i in deviceName.indices) {
            d[i] = "P_ID = ${devices[i].productId} /V_ID =  ${devices[i].vendorId}"
        }
        Log.d(TAG, "getDeviceToUsbPort: $deviceName : ${mDeviceList.size}")
        AlertDialog.Builder(context)
            .setTitle("Select Printer")
            .setItems(d, DialogInterface.OnClickListener() { _, p ->
                mDevice = devices[p]
                Log.d(
                    TAG,
                    "getDeviceToUsbPort: selected device: P_id = ${mDevice?.productId} / V_id = ${mDevice?.vendorId}"
                )
            }).create().show()
        return mDevice
    }

    fun usbPrintOpen(context: Context) {
        executorService.submit(
            TaskOpen(
                usbPrinting,
                mUsbManager,
                mDevice,
                context
            )
        )
    }

    fun usbPrintOpen(context: Context,mDevice: UsbDevice) {
        executorService.submit(
            TaskOpen(
                usbPrinting,
                mUsbManager,
                mDevice,
                context
            )
        )
    }

    fun usbPrintClose() {
        Log.d(TAG, "usbPrintClose: Usb Print Close")
        executorService.submit(
            TaskClose(
                usbPrinting
            )
        )
    }

    fun print(
        c: Context,
        nBitmap: Bitmap,
        nData: String? = null,
        nPrintWidth: Int = 384,
        bCutter: Boolean = true,
        bDrawer: Boolean = false,
        bBeeper: Boolean = true,
        nPrintCount: Int = 1,
        nPrintContent: Int = 2,
        nCompressMethod: Int = 0
    ) {
        executorService.submit(
            TaskPrint(
                pos,
                c,
                nPrintWidth,
                bCutter,
                bDrawer,
                bBeeper,
                nPrintCount,
                nPrintContent,
                nCompressMethod,
                nData,
                nBitmap
            )
        )
    }

    class TaskOpen(
        usb: USBPrinting?,
        usbManager: UsbManager?,
        usbDevice: UsbDevice?,
        context: Context?
    ) : Runnable {
        var usb: USBPrinting? = null
        var usbManager: UsbManager? = null
        var usbDevice: UsbDevice? = null
        var context: Context? = null

        init {
            this.usb = usb
            this.usbManager = usbManager
            this.usbDevice = usbDevice
            this.context = context
        }

        override fun run() {

            Log.d(TAG, "TaskOpen run: ${usbDevice?.deviceName} / ${usb?.IsOpened()} / ")

            usb?.Open(usbManager, usbDevice, context) ?: Log.d(
                TAG,
                "TaskOpen run: usb disconnected"
            )
            if (usb?.IsOpened() == true) {
                Log.d(TAG, "TaskOpen run: usb print is open")
            } else {
                Log.d(TAG, "TaskOpen run: usb print is not open")
            }
        }

    }

    class TaskClose(usb: USBPrinting?) : Runnable {
        var usb: USBPrinting? = null

        init {
            this.usb = usb
        }

        override fun run() {
            usb?.Close() ?: Log.d(TAG, "TaskClose run: usb printer dis connected")
            usb?.SkipAvailable()
        }
    }

    class TaskPrint(
        pos: Pos?, c: Context,
        nPrintWidth: Int = 384,
        bCutter: Boolean = true,
        bDrawer: Boolean = false,
        bBeeper: Boolean = true,
        nPrintCount: Int = 1,
        nPrintContent: Int = 1,
        nCompressMethod: Int = 0,
        nData: String? = null,
        nBitmap: Bitmap? = null
    ) : Runnable {
        private var pos: Pos? = null
        private var context: Context
        private var printWidth: Int
        private var cutter: Boolean
        private var drawer: Boolean
        private var beeper: Boolean
        private var printCount: Int
        private var printContent: Int
        private var compressMethod: Int
        private var data: String?
        private var image: Bitmap?

        init {
            this.pos = pos
            this.context = c
            this.printWidth = nPrintWidth
            this.cutter = bCutter
            this.drawer = bDrawer
            this.beeper = bBeeper
            this.printCount = nPrintCount
            this.printContent = nPrintContent
            this.compressMethod = nCompressMethod
            this.data = nData
            this.image = nBitmap
        }

        override fun run() {
            // TODO Auto-generated method stub
            val bPrintResult: Int = Prints.PrintTicket(
                context,
                pos,
                printWidth,
                cutter,
                drawer,
                beeper,
                printCount,
                printContent,
                compressMethod,
                data,
                image
            )
            val bIsOpened = pos!!.GetIO().IsOpened()
            Log.d(
                TAG,
                "run: PrintResult = ($bPrintResult)  / $bIsOpened"
            )
        }
    }

    class TaskSkip(usb: USBPrinting?) : Runnable {
        var usb: USBPrinting? = null

        init {
            this.usb = usb
        }

        override fun run() {
            usb?.SkipAvailable() ?: Log.d(TAG, "TaskSkip run: usb disconnected")
            Log.d(TAG, "TaskSkip : ${usb?.IsOpened()}")
            usb?.Close()
        }
    }
}