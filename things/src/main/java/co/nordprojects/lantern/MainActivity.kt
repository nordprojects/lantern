package co.nordprojects.lantern

import android.app.Activity
import android.os.Bundle
import android.util.Log
import co.nordprojects.lantern.channels.BlankChannel
import co.nordprojects.lantern.channels.CalendarChannel
import co.nordprojects.lantern.channels.ErrorChannel
import org.json.JSONObject
import java.util.*

/**
 * The main activity coordinates the display of channels, depending on the current orientation
 * and the config.
 */
class MainActivity : Activity() {
    val TAG = MainActivity::class.java.simpleName

    val accelerometer = Accelerometer()
    val accelerometerObserver = Observer { _, _ -> accelerometerUpdated() }
    val appConfigObserver = Observer { _, _ -> appConfigUpdated() }
    val channels = mutableMapOf<Direction, Channel>()
    var visibleChannel: Channel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        accelerometer.addObserver(accelerometerObserver)
        App.instance.config.addObserver(appConfigObserver)

        // TODO(joerick): remove this! 🔥🔥🔥🔥
        App.instance.config.updateWithJson(JSONObject(
                """{
                    "planes": {
                        "up": {
                            "type": "blank"
                        },
                        "forward": {
                            "type": "calendar-countdown"
                        },
                        "down": {
                            "type": "blork"
                        }
                    }
                }"""))

        setContentView(R.layout.main_activity_layout)
        updateChannels()
        Log.d(TAG, "Main activity created.")
    }

    override fun onDestroy() {
        super.onDestroy()
        accelerometer.deleteObserver(accelerometerObserver)
        App.instance.config.deleteObserver(appConfigObserver)
        accelerometer.close()
    }

    override fun onStart() {
        super.onStart()
        updateVisibleChannel()
        accelerometer.startUpdating()
        Log.d(TAG, "Main activity started.")
    }

    override fun onStop() {
        super.onStop()
        accelerometer.stopUpdating()
        Log.d(TAG, "Main activity stopped.")
    }

    private fun accelerometerUpdated() {
        Log.d(TAG, "accelerometer direction updated to ${accelerometer.direction}")
        updateVisibleChannel()
    }

    private fun appConfigUpdated() {
        updateChannels()
        updateVisibleChannel()
    }

    private fun updateChannels() {
        for (direction in Direction.values()) {
            val incomingChannelConfig = App.instance.config.planes[direction]!!
            val prevChannel = channels[direction]

            val needsRefresh =
                    if (prevChannel == null) {
                        true
                    } else {
                        (incomingChannelConfig  != prevChannel.config)
                    }

            if (needsRefresh) {
                val newChannel = newChannelForConfig(incomingChannelConfig)
                channels[direction] = newChannel
                Log.i(TAG, "Channel for ${direction} is now ${newChannel}")
                updateVisibleChannel()
            }
        }
    }

    private fun updateVisibleChannel() {
        val newVisibleChannel = channels[accelerometer.direction]
        if (visibleChannel == newVisibleChannel) {
            return
        }

        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.viewGroup, newVisibleChannel)
        transaction.commit()
    }

    private fun newChannelForConfig(config: ChannelConfiguration): Channel {
        val args = Bundle()
        args.putParcelable(Channel.ARG_CONFIG, config)

        val channel = when (config.type) {
            "calendar-countdown" -> CalendarChannel()
            "blank" -> BlankChannel()
            else -> {
                args.putString(ErrorChannel.ARG_MESSAGE, "Unknown channel type '${config.type}'")
                ErrorChannel()
            }
        }

        channel.arguments = args
        return channel
    }
}
