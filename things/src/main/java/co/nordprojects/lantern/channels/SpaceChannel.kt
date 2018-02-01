package co.nordprojects.lantern.channels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import co.nordprojects.lantern.Channel
import co.nordprojects.lantern.R

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