package com.example.androidthings.lantern

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View
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

    private val rotationDisabled: Boolean by lazy {
        arguments!!.getBoolean(ARG_ROTATION_DISABLED)
    }

    companion object {
        const val ARG_CONFIG = "config"
        const val ARG_ROTATION_DISABLED = "rotationDisabled"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!rotationDisabled) {
            view.rotation = when(config.rotation) {
                Rotation.LANDSCAPE -> 0F
                Rotation.LANDSCAPE_UPSIDE_DOWN -> 180F
            }
        }
    }
}
