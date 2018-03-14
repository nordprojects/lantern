package com.example.androidthings.lantern.channels.config


import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.androidthings.lantern.App
import com.example.androidthings.lantern.R
import com.example.androidthings.lantern.shared.CastDiscoveryManager
import kotlinx.android.synthetic.main.fragment_now_playing_cast.*
import kotlinx.android.synthetic.main.item_row_cast_device.view.*


class NowPlayingCastFragment : Fragment() {

    companion object {
        val TAG: String = NowPlayingCastFragment::class.java.simpleName
    }

    private val castDiscoveryManager = CastDiscoveryManager(App.instance)
    private val castDevices get() = castDiscoveryManager.devices
    private var selectedDevice: CastDiscoveryManager.CastDevice? = null
    var listener: OnCastDeviceSelectedListener? = null

    interface OnCastDeviceSelectedListener {
        fun onCastDeviceSelectedSelected(device: CastDiscoveryManager.CastDevice?)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_now_playing_cast, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setChannelButton.setOnClickListener { listener?.onCastDeviceSelectedSelected(selectedDevice) }
        recyclerView.adapter = CastDevicesListAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this.context!!)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        val activity = activity
        if (activity is OnCastDeviceSelectedListener) listener = activity
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

    private fun deviceTapped(device: CastDiscoveryManager.CastDevice) {
        selectedDevice = device
        update()
    }

    private fun update() {
        Log.i(TAG, "Cast devices updated: $castDevices")
        recyclerView?.adapter?.notifyDataSetChanged()
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
}// Required empty public constructor
