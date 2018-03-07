package com.example.androidthings.lantern

import android.support.v4.app.Fragment
import com.example.androidthings.lantern.shared.ChannelConfiguration

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
}
