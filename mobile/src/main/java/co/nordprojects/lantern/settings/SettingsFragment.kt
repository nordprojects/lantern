package co.nordprojects.lantern.settings


import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.nordprojects.lantern.App

import co.nordprojects.lantern.R
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.android.synthetic.main.item_row_channel.*


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
        Log.i(TAG, "onDisconnectClicked")
    }

    private fun onResetDeviceClicked() {
        Log.i(TAG, "onResetDeviceClicked")
    }

    private fun onLearnMoreClicked() {
        Log.i(TAG, "onLearnMoreClick")
    }

}
