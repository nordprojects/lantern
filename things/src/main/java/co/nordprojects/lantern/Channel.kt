package co.nordprojects.lantern

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Fragment
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet

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
