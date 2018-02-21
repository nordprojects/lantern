package co.nordprojects.lantern

import android.app.Fragment
import co.nordprojects.lantern.shared.ChannelConfiguration

/**
 * Channel base class. Subclass and override fragment lifecycle methods like onViewCreate to
 * add content.
 */
open class Channel : Fragment() {
    val config: ChannelConfiguration by lazy {
        arguments.getParcelable<ChannelConfiguration>(ARG_CONFIG)
    }

    companion object {
        const val ARG_CONFIG = "config"
    }
}
