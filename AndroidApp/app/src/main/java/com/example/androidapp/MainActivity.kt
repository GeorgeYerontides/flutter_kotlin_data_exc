package com.example.androidapp

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

private const val FLUTTER_ENGINE_ID = "module_flutter_engine"
private const val CHANNEL_NAME = "channel_test"  // Replace with your channel name

class MainActivity : AppCompatActivity() {
    lateinit var flutterEngine: FlutterEngine
    private var latestCounter = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Instantiate a FlutterEngine
        flutterEngine = FlutterEngine(this)

        // Start executing Dart code to pre-warm the FlutterEngine
        flutterEngine.dartExecutor.executeDartEntrypoint(
            DartExecutor.DartEntrypoint.createDefault()
        )

        // Cache the FlutterEngine to be used by FlutterActivity
        FlutterEngineCache
            .getInstance()
            .put(FLUTTER_ENGINE_ID, flutterEngine)

        val myButton = findViewById<Button>(R.id.myButton)
        val counterTextView = findViewById<TextView>(R.id.counterTextView)

        myButton.setOnClickListener {
            // Send initial data to Flutter when the button is clicked
            MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL_NAME)
                .invokeMethod("sendDataToFlutter", mapOf("data" to "Hello from Android"))
            Log.d("MainActivity", "Data sent to Flutter")

            // Launch FlutterActivity with the cached FlutterEngine
            startActivityForResult(
                FlutterActivity
                    .withCachedEngine(FLUTTER_ENGINE_ID)
                    .build(this),
                1
            )
        }

        // Set up the method channel on the Android side
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL_NAME)
            .setMethodCallHandler(object : MethodCallHandler {
                override fun onMethodCall(call: MethodCall, result: Result) {
                    if (call.method == "updateCounter") {
                        val counter = call.argument<Int>("counter")
                        // Handle the counter value (e.g., update UI)
                        latestCounter = counter ?: 0
                        counterTextView.text = "Latest Counter: $latestCounter"
                        Log.d("MainActivity", "Received counter value from Flutter: $latestCounter")
                        result.success("Counter value received by Android")
                    } else {
                        result.notImplemented()
                    }
                }
            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            // FlutterActivity has closed
            Log.d("MainActivity", "FlutterActivity closed")

            // Display the latest counter value in your app (e.g., update UI)
            Log.d("MainActivity", "Latest counter value: $latestCounter")
        }
    }
}
