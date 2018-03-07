package com.example.androidthings.lantern.channels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.androidthings.lantern.App
import com.example.androidthings.lantern.Channel
import com.example.androidthings.lantern.R
import com.google.android.things.AndroidThings
import kotlinx.android.synthetic.main.info_channel.*
import kotlinx.android.synthetic.main.main_activity_layout.*
import java.net.Inet4Address
import java.net.NetworkInterface

/**
 * Displays information about the projector.
 *
 * Created by joerick on 01/03/18.
 */
class InfoChannel: Channel() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.info_channel, viewGroup, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nameTextView.text = "‘${App.instance.advertisingName}’"
        ipAddressTextView.text = getIpAddresses().joinToString(separator = "\n")
        androidThingsVersionTextView.text = "Android Things ${AndroidThings.RELEASE}"
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        lanternVersionTextView.text = with(packageInfo) {
            "Lantern v$versionName ($versionCode)"
        }
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
}