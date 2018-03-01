package com.example.androidthings.lantern

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.google.android.things.AndroidThings
import kotlinx.android.synthetic.main.launch_activity_layout.*
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.*
import kotlin.concurrent.fixedRateTimer
import kotlin.concurrent.timer

/**
 * Created by joerick on 01/03/18.
 */
class LaunchActivity: Activity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.launch_activity_layout)

        nameTextView.text = "’${App.instance.advertisingName}‘"
        ipAddressTextView.text = getIpAddresses().joinToString(separator = "\n")
        androidThingsVersionTextView.text = "Android Things ${AndroidThings.getVersionString()}"
        lanternVersionTextView.text = with(packageManager.getPackageInfo(packageName, 0)) {
            "Lantern v$versionName ($versionCode)"
        }

        Log.d(TAG, "Launch activity created.")
    }

    override fun onStart() {
        super.onStart()

        Handler().postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }, 10000)
    }

    private fun getIpAddresses(): List<String> {
        val result = mutableListOf<String>()
        for (networkInterface in NetworkInterface.getNetworkInterfaces()) {
            for (address in networkInterface.inetAddresses) {
                if (address is Inet4Address && !address.isLoopbackAddress) {
                    result.add(address.hostAddress)
                }
            }
        }
        return result
    }

    companion object {
        val TAG = LaunchActivity::class.java.simpleName!!
    }
}