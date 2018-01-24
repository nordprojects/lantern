package co.nordprojects.lantern

import android.app.Fragment

/**
 * Created by joerick on 18/01/18.
 */
open class Channel(): Fragment() {
    val config: ChannelConfiguration by lazy {
        arguments.getParcelable<ChannelConfiguration>(ARG_CONFIG)
    }

    companion object {
        val ARG_CONFIG = "config"
    }
}
