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

    @SuppressLint("ValidFragment")
    constructor(config: ChannelConfiguration) : this() {
        val args = Bundle()
        args.putParcelable("config", config)
        this.arguments = args
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        config
    }

    override fun onStart() {
        super.onStart()
//        config = intent.getStringExtra("configJson")
    }
}