package com.example.androidthings.lantern.channels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.example.androidthings.lantern.Channel
import com.example.androidthings.lantern.R

/**
 * Created by Michael Colville on 31/01/2018.
 */
class SpaceChannel: Channel() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return ImageView(this.activity).apply {
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            setImageResource(R.drawable.space)
        }
    }
}