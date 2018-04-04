package com.example.androidthings.lantern

import android.support.v4.app.Fragment
import com.example.androidthings.lantern.shared.ChannelConfiguration
import com.example.androidthings.lantern.shared.Rotation

/**
 * Channel base class. Subclass and override fragment lifecycle methods like onViewCreate to
 * add content.
 */
open class Channel : Fragment() {
    val config: ChannelConfiguration by lazy {
        arguments!!.getParcelable<ChannelConfiguration>(ARG_CONFIG)
    }

    companion object {
        const val ARG_CONFIG = "config"
    }

    override fun onStart() {
        super.onStart()
        updateRotation()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) updateRotation()
    }

    private fun updateRotation() {
       val view = view ?: return
       view.rotation = when(config.rotation) {
            Rotation.LANDSCAPE -> 0F
            Rotation.LANDSCAPE_UPSIDE_DOWN -> 180F
        }
    }
}
