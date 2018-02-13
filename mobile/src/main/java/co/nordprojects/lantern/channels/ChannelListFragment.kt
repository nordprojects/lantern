package co.nordprojects.lantern.channels


import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.nordprojects.lantern.App

import co.nordprojects.lantern.R
import co.nordprojects.lantern.search.EndpointAdapter
import co.nordprojects.lantern.search.ProjectorListFragment
import co.nordprojects.lantern.shared.ChannelInfo
import kotlinx.android.synthetic.main.fragment_projector_select.*
import java.nio.channels.Channel
import java.util.Observer


/**
 * A simple [Fragment] subclass.
 */
class ChannelListFragment : Fragment() {

    private var onChannelSelectedListener: OnChannelSelectedListener? = null
    private val projectorObserver = Observer { _, _ -> update() }

    companion object {
        var TAG: String = ProjectorListFragment::class.java.simpleName
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
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        val projector = App.instance.projector
        if (projector != null) {
            recyclerView.adapter = ChannelListAdapter(projector.availableChannels, onChannelSelectedListener)
        }
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

    fun update() {
        recyclerView.adapter = ChannelListAdapter(App.instance.projector!!.availableChannels, onChannelSelectedListener)
        recyclerView.invalidate()
    }
}
