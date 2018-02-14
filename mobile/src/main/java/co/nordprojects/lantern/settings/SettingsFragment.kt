package co.nordprojects.lantern.settings


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.nordprojects.lantern.App

import co.nordprojects.lantern.R
import co.nordprojects.lantern.search.ProjectorSearchActivity
import kotlinx.android.synthetic.main.fragment_settings.*


class SettingsFragment : Fragment() {

    companion object {
        val TAG: String = SettingsFragment::class.java.simpleName
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        deviceDetails.setOnClickListener { onDeviceDetailsClicked() }
        disconnect.setOnClickListener { onDisconnectClicked() }
        resetDevice.setOnClickListener { onResetDeviceClicked() }
        learnMore.setOnClickListener { onLearnMoreClicked() }

        updateDeviceDetails()
    }

    private fun updateDeviceDetails() {
        App.instance.projector?.let {
            textViewDeviceDetailsDesc.text = "Device name: ${it.name}\nDevice ID: ${it.deviceID}"
        }
    }

    private fun onDeviceDetailsClicked() {
        Log.i(TAG, "onDeviceDetailsClicked")
        // TODO - let user change device name once option available in firmware
    }

    private fun onDisconnectClicked() {
        val intent = Intent("disconnect_from_projector")
        activity?.sendBroadcast(intent)
        activity?.finish()
    }

    private fun onResetDeviceClicked() {
        App.instance.configClient.activeConnection?.sendResetDevice()
        activity?.finish()
    }

    private fun onLearnMoreClicked() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://developer.android.com/things/index.html"))
        startActivity(intent)
    }

}
