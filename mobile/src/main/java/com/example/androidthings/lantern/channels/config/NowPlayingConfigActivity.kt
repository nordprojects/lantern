package com.example.androidthings.lantern.channels.config

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.androidthings.lantern.R
import com.example.androidthings.lantern.shared.CastDiscoveryManager
import kotlinx.android.synthetic.main.activity_now_playing_config.*
import kotlinx.android.synthetic.main.item_row_cast_device.view.*


class NowPlayingConfigActivity: ChannelConfigActivity() {
    companion object {
        val TAG = NowPlayingConfigActivity::class.java.simpleName
    }
    private val castDiscoveryManager = CastDiscoveryManager(this)
    private val castDevices get() = castDiscoveryManager.devices
    private var selectedDevice: CastDiscoveryManager.CastDevice? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_now_playing_config)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationIcon(R.drawable.back_chevron)
        toolbar.setNavigationOnClickListener { finish() }
        title = "Customize"

        setChannelButton.setOnClickListener { finishWithConfigUpdate() }
        recyclerView.adapter = CastDevicesListAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    override fun onStart() {
        super.onStart()

        castDiscoveryManager.listener = object : CastDiscoveryManager.Listener {
            override fun devicesUpdated() {
                update()
            }
        }
        castDiscoveryManager.startDiscovering()
    }

    override fun onStop() {
        super.onStop()
        castDiscoveryManager.listener = null
        castDiscoveryManager.stopDiscovering()
    }

    private fun update() {
        Log.i(TAG, "Cast devices updated: $castDevices")
        recyclerView?.adapter?.notifyDataSetChanged()
    }

    private fun deviceTapped(device: CastDiscoveryManager.CastDevice) {
        selectedDevice = device
        update()
    }

    override fun finishWithConfigUpdate() {
        val device = selectedDevice ?: return
        config.settings.put("castId", device.id)
        config.settings.put("subtitle", "‘${device.name}’")
        config.settings.put("subtitleVia", "via Chromecast Audio")
        super.finishWithConfigUpdate()
    }

    private inner class CastDevicesListAdapter : RecyclerView.Adapter<CastDevicesListAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_row_cast_device, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int {
            return castDevices.count()
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val device = castDevices[position]
            holder.bindDevice(device)
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            fun bindDevice(device: CastDiscoveryManager.CastDevice) {
                itemView.nameTextView.text = device.name
                itemView.radioButton.isChecked = (device === selectedDevice)
                itemView.setOnClickListener { deviceTapped(device) }
            }
        }
    }
}
