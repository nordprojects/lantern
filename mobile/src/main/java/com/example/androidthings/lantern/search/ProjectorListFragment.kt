package com.example.androidthings.lantern.search


import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.androidthings.lantern.R
import kotlinx.android.synthetic.main.fragment_projector_select.*
import android.support.v7.widget.RecyclerView
import android.util.Log
import com.example.androidthings.lantern.App
import com.example.androidthings.lantern.configuration.Discovery
import kotlinx.android.synthetic.main.item_row_projector.view.*
import java.util.*


class ProjectorListFragment : Fragment() {

    private var onProjectorSelectedListener: OnProjectorSelectedListener? = null
    private val endpointObserver: Observer = Observer { _, _ -> onEndpointsUpdated() }

    interface OnProjectorSelectedListener {
        fun onProjectorSelected(endpoint: Discovery.Endpoint)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_projector_select, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = EndpointAdapter(App.instance.discovery.endpoints, onProjectorSelectedListener)

        App.instance.discovery.addObserver(endpointObserver)
    }

    private fun onEndpointsUpdated() {
        recyclerView.adapter.notifyDataSetChanged()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        val activity = activity
        if (activity is OnProjectorSelectedListener) onProjectorSelectedListener = activity
    }

    override fun onDestroy() {
        super.onDestroy()
        App.instance.discovery.deleteObserver(endpointObserver)
    }

    class EndpointAdapter(private val endpoints: ArrayList<Discovery.Endpoint>,
                          private val listener: ProjectorListFragment.OnProjectorSelectedListener?):
            RecyclerView.Adapter<EndpointAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_row_projector, parent, false)
            return ViewHolder(view, listener)
        }

        override fun getItemCount(): Int {
            return endpoints.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bindEndpoint(endpoints[position])
        }

        class ViewHolder(var view: View,
                         private val listener: ProjectorListFragment.OnProjectorSelectedListener?):
                RecyclerView.ViewHolder(view) {

            var endpoint: Discovery.Endpoint? = null

            fun bindEndpoint(endpoint: Discovery.Endpoint) {
                this.endpoint = endpoint
                view.endpointNameTextView.text = "${endpoint.info.endpointName}"
                view.idTextView.text = "Lantern: ${endpoint.id}"
                view.setOnClickListener { listener?.onProjectorSelected(endpoint) }
            }
        }
    }
}
