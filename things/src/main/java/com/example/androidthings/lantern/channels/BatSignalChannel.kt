package com.example.androidthings.lantern.channels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.example.androidthings.lantern.Channel
import com.example.androidthings.lantern.R

/**
 * Shows the bat signal!
 */
class BatSignalChannel : Channel() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return ImageView(this.activity).apply {
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            setImageResource(R.drawable.batdroid_signal)
        }
    }
}
