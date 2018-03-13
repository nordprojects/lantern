package com.example.androidthings.lantern.channels.spaceporthole

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.androidthings.lantern.Channel
import com.example.androidthings.lantern.R
import processing.android.PFragment


class SpacePortholeChannel : Channel() {
    private val sketch by lazy {
        SpacePortholeApplet().apply {
            latitude = config.settings.optDouble("latitude", 0.0).toFloat()
            longitude = config.settings.optDouble("longitude", 0.0).toFloat()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.space_porthole_channel, container, false);
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragmentManager!!.beginTransaction()
                .add(R.id.sketchViewGroup, PFragment(sketch))
                .commit()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden) {
            sketch.surface.pauseThread()
        } else {
            sketch.surface.resumeThread()
        }
    }
}