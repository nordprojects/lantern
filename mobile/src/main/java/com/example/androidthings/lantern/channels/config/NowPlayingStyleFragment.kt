package com.example.androidthings.lantern.channels.config


import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.androidthings.lantern.R
import kotlinx.android.synthetic.main.fragment_now_playing_style.*


class NowPlayingStyleFragment : Fragment() {

    var listener: OnStyleSelectedListener? = null

    interface OnStyleSelectedListener {
        fun onStyleSelected(style: String)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_now_playing_style, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        stackedButton.setOnClickListener({ listener?.onStyleSelected("stacked") })
        scrollingButton.setOnClickListener({ listener?.onStyleSelected("scrolling")})
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        val activity = activity
        if (activity is OnStyleSelectedListener) listener = activity
    }
}
