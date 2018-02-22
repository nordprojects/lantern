package com.example.androidthings.lantern.settings


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.androidthings.lantern.App

import com.example.androidthings.lantern.R
import com.example.androidthings.lantern.home.HomeActivity
import kotlinx.android.synthetic.main.fragment_settings.*
import android.support.v7.app.AlertDialog
import android.text.InputType
import android.widget.EditText
import java.util.Observer


class SettingsFragment : Fragment() {

    companion object {
        val TAG: String = SettingsFragment::class.java.simpleName
    }
    private val projectorConfigObserver = Observer { _, _ -> updateDeviceDetails() }

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
        val deviceID = App.instance.client.activeConnection?.endpointId ?: "N/A"
        App.instance.projector?.let {
            textViewDeviceDetailsDesc.text = "Device name: ${it.name}\nDevice ID: ${deviceID}"
        }
    }

    override fun onResume() {
        super.onResume()
        App.instance.projector?.addObserver(projectorConfigObserver)
    }

    override fun onPause() {
        super.onPause()
        App.instance.projector?.deleteObserver(projectorConfigObserver)
    }

    private fun onDeviceDetailsClicked() {
        // TODO - replace with xml layout
        val builder = AlertDialog.Builder(context!!)
        builder.setTitle("Pick a name for your Lantern")
        val input = EditText(context!!)
        input.setPadding(50,50,50,50)
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)
        builder.setPositiveButton("Change Name",  { _, _ ->
            App.instance.client.activeConnection?.sendSetName(input.text.toString())
        })
        builder.setNegativeButton("Cancel",  { dialog, _ ->
            dialog.cancel()
        })
        builder.show()
    }

    private fun onDisconnectClicked() {
        val intent = Intent(HomeActivity.DISCONNECT_ACTIVITY)
        activity?.sendBroadcast(intent)
        activity?.finish()
    }

    private fun onResetDeviceClicked() {
        App.instance.client.activeConnection?.sendResetDevice()
        activity?.finish()
    }

    private fun onLearnMoreClicked() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://developer.android.com/things/index.html"))
        startActivity(intent)
    }

}
