package co.nordprojects.lantern

import android.app.Activity
import android.app.Fragment
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet

/**
 * Created by joerick on 18/01/18.
 */
class BaseChannel(): Fragment() {
    lateinit var config: ChannelConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        config
    }

    override fun onStart() {
        super.onStart()
//        config = intent.getStringExtra("configJson")
    }
}