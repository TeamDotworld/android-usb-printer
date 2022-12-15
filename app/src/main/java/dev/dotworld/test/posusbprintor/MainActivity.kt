package dev.dotworld.test.posusbprintor

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import dev.dotworld.test.posusbprintor.databinding.ActivityMainBinding
import dev.dotworld.test.posusbprintor.utils.generateImage
import dev.dotworld.test.usbprinter.PosUsbPrinter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view:View = binding!!.root
        setContentView(view)
        PosUsbPrinter.posSetup(this)

        binding.printerScan.setOnClickListener {
            val device=PosUsbPrinter.searchAndSelectUsbPost(this@MainActivity)
            Log.d(TAG, "onCreate: device =$device ")
            binding.printerId.text="${device?.productId} / ${device?.vendorId}"
        }
        binding.printerSetting.setOnClickListener {
            val device = PosUsbPrinter.usbDevice()
            binding.printerId.text="${device?.productId} / ${device?.vendorId}"
        }
        binding.printOpen.setOnClickListener {
            PosUsbPrinter.usbPrintOpen(this)
        }
        binding.print.setOnClickListener {
            printReceipt(this)
        }
        binding.createBitmap.setOnClickListener {
            createBitmap(this)
        }
        binding.printerClose.setOnClickListener {
            PosUsbPrinter.usbPrintClose()
        }
    }


    fun printReceipt(activity: Activity, data: Map<String, Any> = mapOf(
        "date" to Date().toLocaleString(),
        "mode" to 12,
        "amount" to 13000,
        "customerId" to "12D233",
        "transactionId" to "12DDDD3DQE",
        "advance" to "2000"
    )) {
        Log.d(TAG, "printReceipt: start")
        val bitmap: Bitmap?
        GlobalScope.launch {
            val bitmap: Bitmap? = generateImage(activity.applicationContext, data)
            if (bitmap != null){
                runOnUiThread(){
                    binding.htmlToBitmap.setImageBitmap(bitmap)
                }
            }
            bitmap?.let {
                Log.d(TAG, "printReceipt.print: start")
                PosUsbPrinter.print(activity.applicationContext,bitmap)
            }
        }

    }

    fun createBitmap(activity: Activity){
        val data: Map<String, Any> = mapOf(
            "date" to Date().toLocaleString(),
            "mode" to 12,
            "amount" to 13000,
            "customerId" to "12D233",
            "transactionId" to "12DDDD3DQE",
            "advance" to "2000")
        var bitmap: Bitmap? = null
        Log.d(TAG, "createBitmap: bitmap1 = $bitmap")
        GlobalScope.launch{
             bitmap = generateImage(activity.applicationContext, data)
            Log.d(TAG, "createBitmap: bitmap2 = $bitmap")
            if (bitmap != null){
                Log.d(TAG, "createBitmap: bitmap size = ${(bitmap?.byteCount?.div(1024))}")
                runOnUiThread(){
                    binding.htmlToBitmap.setImageBitmap(bitmap)
                }
            }
        }


    }
    
    companion object{
        private const val TAG:String = "MainActivity"
        private lateinit var mUsbManager: UsbManager
        private var mDevice: UsbDevice? = null
        private lateinit var mDeviceList: HashMap<String, UsbDevice>
    }
}