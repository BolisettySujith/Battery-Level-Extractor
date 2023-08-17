package com.example.flutter_native_connect_practice

import android.content.BroadcastReceiver
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PersistableBundle
import android.util.Log
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.delay
import java.lang.Thread.sleep

class MainActivity: FlutterActivity() {
    private val BATTERY_CHANNEL = "com.example.flutter_native_connect_practice/battery"
    private val EVENT_CHANNEL = "com.example.flutter_native_connect_practice/charging"

    private lateinit var channel : MethodChannel
    private lateinit var eventChannel: EventChannel

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        channel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, BATTERY_CHANNEL)
        eventChannel = EventChannel(flutterEngine.dartExecutor.binaryMessenger, EVENT_CHANNEL)

        eventChannel.setStreamHandler(MyStreamHandler(context))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Handler(Looper.getMainLooper()).postDelayed({
            val batteryLevel = getBatteryLevel()
            channel.invokeMethod("reportBatteryLevel", batteryLevel)
        }, 0)
    }

    private fun getBatteryLevel(): Int {
        val batteryLevel : Int = if(VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            val batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

        } else {
            val intent = ContextWrapper(applicationContext).registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            intent!!.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) * 100 / intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        }


        return batteryLevel
    }
}


class MyStreamHandler(private val context: Context) : EventChannel.StreamHandler {

    private var receiver: BroadcastReceiver? = null

    override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
        if(events == null) return

        receiver = initReceiver(events)

        context.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    private fun initReceiver(events: EventChannel.EventSink): BroadcastReceiver {
        return object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                Log.d("MainApp", status.toString())
                when(status) {
                    BatteryManager.BATTERY_STATUS_CHARGING -> events.success("Battery is Charging")
                    BatteryManager.BATTERY_STATUS_FULL -> events.success("Battery is Full")
                    BatteryManager.BATTERY_STATUS_NOT_CHARGING -> events.success("Battery is Not Charging")
                }
            }
        }
    }

    override fun onCancel(arguments: Any?) {
        context.unregisterReceiver(receiver)
        receiver = null
    }

}