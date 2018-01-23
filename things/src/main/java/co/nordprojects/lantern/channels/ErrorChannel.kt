package co.nordprojects.lantern.channels

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import co.nordprojects.lantern.Channel
import co.nordprojects.lantern.ChannelConfiguration


/**
 * A channel that presents the user with an error message.
 *
 * Created by joerick on 23/01/18.
 */
@SuppressLint("ValidFragment")
class ErrorChannel(config: ChannelConfiguration) : Channel(config) {
    val errorMessage: String
        get() = config.settings.getString("message")

    constructor(errorMessage: String) : this(ChannelConfiguration.error(errorMessage)) {
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = TextView(this.context)
        view.text = errorMessage
        view.gravity = Gravity.CENTER
        view.setTextColor(Color.RED)
        view.setBackgroundColor(Color.BLACK)
        return view
    }
}
