package com.example.androidthings.lantern.channels


import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.androidthings.lantern.App
import com.example.androidthings.lantern.R
import com.example.androidthings.lantern.color
import com.example.androidthings.lantern.shared.ChannelInfo
import com.example.androidthings.lantern.shared.Direction
import kotlinx.android.synthetic.main.fragment_channel_list.*
import java.util.*


class ChannelListFragment : Fragment() {

    private var onChannelSelectedListener: OnChannelSelectedListener? = null
    private val projectorObserver = Observer { _, _ -> update() }
    private val direction: Direction by lazy {
            val directionString = arguments?.getString(ChannelsListActivity.ARG_DIRECTION)
            Direction.valueOf(directionString ?: "")
        }


    interface OnChannelSelectedListener {
        fun onChannelSelected(channel: ChannelInfo)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_channel_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerViewCurrent.setHasFixedSize(true)
        recyclerViewCurrent.layoutManager = LinearLayoutManager(activity)


        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(activity)

        val projector = App.instance.projector
        val availableChannels = App.instance.client.activeConnection?.availableChannels
        if (projector != null && availableChannels != null) {
            val channel = projector.planes[direction]!!

            val selected = availableChannels.filter {
                it.id == channel.type
            }
            recyclerViewCurrent.adapter = ChannelListAdapter(selected,
                    onChannelSelectedListener,
                    direction)

            val other = availableChannels.filter {
                it.id != channel.type
            }
            recyclerView.adapter = ChannelListAdapter(other,
                    onChannelSelectedListener,
                    direction)
        }

        updateHeader()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        val activity = activity
        if (activity is OnChannelSelectedListener) onChannelSelectedListener = activity
    }

    override fun onResume() {
        super.onResume()
        App.instance.projector?.addObserver(projectorObserver)
    }

    override fun onPause() {
        super.onPause()
        App.instance.projector?.deleteObserver(projectorObserver)
    }

    private fun updateHeader() {
        val projectorDirectionAngle: Float = when(direction) {
            Direction.UP -> -90F
            Direction.FORWARD -> 0F
            Direction.DOWN -> 90F
        }

        val directionText: String = when(direction) {
            Direction.UP -> "upwards"
            Direction.FORWARD -> "forwards"
            Direction.DOWN -> "downwards"
        }

        val text = "Choose content to project"
        val spannable = SpannableString("$text $directionText")
        spannable.setSpan(ForegroundColorSpan(Color.BLACK), 0, text.length, 0)
        spannable.setSpan(ForegroundColorSpan(ContextCompat.getColor(context!!, direction.color)),
                text.length + 1, text.length + 1 + directionText.length, 0)
        direction_text.text = spannable
        projectorDirection.rotation = projectorDirectionAngle
    }

    private fun update() {
        recyclerView.adapter.notifyDataSetChanged()
    }
}
