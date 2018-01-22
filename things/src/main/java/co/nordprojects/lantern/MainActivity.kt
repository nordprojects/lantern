package co.nordprojects.lantern

import android.app.Activity
import android.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import java.util.*


class MainActivity : Activity() {
    val TAG = MainActivity::class.java.simpleName

    lateinit var frameLayout: FrameLayout
    val accelerometer = Accelerometer()
    val accelerometerObserver = Observer { _, _ -> accelerometerUpdated() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        frameLayout = FrameLayout(this)
        frameLayout.foregroundGravity = 77 // 'fill'

        accelerometer.addObserver(accelerometerObserver)

        setContentView(frameLayout)
        Log.d(TAG, "Main activity created.")
    }

    override fun onDestroy() {
        super.onDestroy()
        accelerometer.deleteObserver(accelerometerObserver)
        accelerometer.close()
    }

    override fun onStart() {
        super.onStart()
        accelerometer.startUpdating()
        Log.d(TAG, "Main activity started.")
    }

    override fun onStop() {
        super.onStop()
        accelerometer.stopUpdating()
        Log.d(TAG, "Main activity stopped.")
    }

    private fun accelerometerUpdated() {
        Log.d(TAG, "accelerometer direction updated to ${accelerometer.direction}")
    }
}
