package com.example.androidthings.lantern

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import com.example.androidthings.lantern.channels.InfoChannel
import com.example.androidthings.lantern.shared.ChannelConfiguration
import com.google.android.things.device.DeviceManager
import kotlinx.android.synthetic.main.launch_activity_layout.*

/**
 * Created by joerick on 01/03/18.
 */
class LaunchActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.launch_activity_layout)
    }

    override fun onStart() {
        super.onStart()

        try {
            App.instance.accelerometer
        } catch (error: Exception) {
            timeRemainingTextView.text = ""
            errorMessageTextView.text = "Unable to detect accelerometer. Please power off the device and check the connection."
            resolutionErrorViewGroup.visibility = View.VISIBLE
            return
        }

        if (isInTheCorrectResolution()) {
            // load the info channel and hold for 10 seconds
            val config = ChannelConfiguration("info")
            val infoChannel = ChannelsRegistry.newChannelForConfig(config)
            supportFragmentManager.beginTransaction()
                    .add(R.id.infoChannelViewGroup, infoChannel)
                    .commit()

            val holdDuration = if (intent.extras?.get("quickStart") as? Boolean == true) {
                Log.i(TAG, "Started with quickStart flag. Skipping startup pause.")
                1000L // 1 second
            } else {
                10000L // 10 seconds
            }

            Handler().postDelayed({
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }, holdDuration)
        } else {
            // show the resolution error message, countdown and reboot after 10 seconds
            resolutionErrorViewGroup.visibility = View.VISIBLE
            val countDownTimer = object : CountDownTimer(10000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    timeRemainingTextView.text = "${millisUntilFinished/1000}"
                }
                override fun onFinish() {
                    timeRemainingTextView.text = "0"
                    DeviceManager.getInstance().reboot()
                }
            }
            countDownTimer.start()
        }
    }

    private fun isInTheCorrectResolution(): Boolean {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        Log.d(TAG, "Checking screen resolution, height is ${displayMetrics.heightPixels}px")

        return (displayMetrics.heightPixels >= 540)
    }

    companion object {
        val TAG = LaunchActivity::class.java.simpleName!!
    }
}