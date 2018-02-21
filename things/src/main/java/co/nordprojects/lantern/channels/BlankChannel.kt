package co.nordprojects.lantern.channels

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.nordprojects.lantern.Channel

/**
 * A full black channel.
 *
 * Created by joerick on 23/01/18.
 */
class BlankChannel() : Channel() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return View(this.context).apply {
            setBackgroundColor(Color.BLACK)
        }
    }
}
