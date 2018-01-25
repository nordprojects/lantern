package co.nordprojects.lantern.channels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.nordprojects.lantern.Channel
import co.nordprojects.lantern.R

class NowPlayingChannel() : Channel() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.now_playing_channel, container, false)
    }
}
