package co.nordprojects.lantern.channels

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import co.nordprojects.lantern.Channel

/**
 * Projects a simple message.
 *
 * Config parameters:
 *   - "message"
 *         The message to display.
 */
class MessageChannel : Channel() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = TextView(this.context)
        view.text = config.settings.optString("message", "[message parameter missing]")
        view.textSize = 30F
        view.gravity = Gravity.CENTER
        view.setTextColor(Color.WHITE)
        view.setBackgroundColor(Color.BLACK)
        return view
    }
}
