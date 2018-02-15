package co.nordprojects.lantern.channels

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.nordprojects.lantern.R
import co.nordprojects.lantern.shared.ChannelInfo
import co.nordprojects.lantern.shared.Direction
import kotlinx.android.synthetic.main.item_row_channel.view.*

/**
 * Created by Michael Colville on 31/01/2018.
 */

class ChannelListAdapter(private val channels: List<ChannelInfo>,
                         private val listener: ChannelListFragment.OnChannelSelectedListener?,
                         private val direction: Direction):
        RecyclerView.Adapter<ChannelListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent?.context)
                .inflate(R.layout.item_row_channel, parent, false)
        return ViewHolder(view, listener, direction)
    }

    override fun getItemCount(): Int {
        return channels.size
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        holder?.bindChannel(channels[position])
    }

    class ViewHolder(var view: View,
                     private val listener: ChannelListFragment.OnChannelSelectedListener?,
                     private val direction: Direction):
            RecyclerView.ViewHolder(view) {

        private var channel: ChannelInfo? = null

        fun bindChannel(channel: ChannelInfo) {
            this.channel = channel
            view.apply {
                channelNameTextView.text = "${channel.name}"
                descriptionTextView.text = "${channel.description}"
                setOnClickListener { listener?.onChannelSelected(channel) }
                selectButton.setTextColor(ContextCompat.getColor(view.context, direction.color))
                selectButton.setOnClickListener { listener?.onChannelSelected(channel) }
                bannerImageView.setImageURI(channel.imageUri)
            }
        }
    }
}
