package com.example.androidthings.lantern

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import com.example.androidthings.lantern.channels.InfoChannel
import com.google.android.things.device.DeviceManager
import kotlinx.android.synthetic.main.launch_activity_layout.*
import java.util.*
import kotlin.concurrent.fixedRateTimer

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

        if (isInTheCorrectResolution()) {
            // load the info channel and hold for 10 seconds
            supportFragmentManager.beginTransaction()
                    .add(R.id.infoChannelViewGroup, InfoChannel())
                    .commit()

            Handler().postDelayed({
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }, 10000)
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