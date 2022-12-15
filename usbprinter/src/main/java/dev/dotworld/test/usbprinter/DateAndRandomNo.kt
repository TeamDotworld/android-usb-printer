package dev.dotworld.test.usbprinter

import android.content.Context
import android.util.Log
import java.io.StringReader
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*



public var random:Int = Random().nextInt(100) + 20

fun getDateAnDTime():String{
    val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
    val dateTime = Date()
    return formatter.format(dateTime)
}
