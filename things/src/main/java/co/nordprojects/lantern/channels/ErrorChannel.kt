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
 * A channel that presents the user with an error message.
 *
 * Created by joerick on 23/01/18.
 */
class ErrorChannel : Channel() {
    val errorMessage: String
        get() = arguments.getString(ARG_MESSAGE)

    companion object {
        val ARG_MESSAGE = "message"

        fun newInstance(errorMessage: String): ErrorChannel {
            val channel = ErrorChannel()
            channel.arguments = Bundle().apply {
                putString(ARG_MESSAGE, errorMessage)
            }
            return channel
        }
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
