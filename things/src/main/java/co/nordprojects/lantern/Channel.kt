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
    lateinit var config: ChannelConfiguration
    private val CONFIG_ARGS_KEY = "config"

    @SuppressLint("ValidFragment")
    constructor(config: ChannelConfiguration) : this() {
        val args = Bundle()
        args.putParcelable(CONFIG_ARGS_KEY, config)
        this.arguments = args
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        config = arguments.getParcelable<ChannelConfiguration>(CONFIG_ARGS_KEY)
    }
}
