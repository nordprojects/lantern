package co.nordprojects.lantern

import android.app.Activity
import android.os.Bundle
import android.util.Log
import co.nordprojects.lantern.channels.BlankChannel
import co.nordprojects.lantern.channels.CalendarChannel
import co.nordprojects.lantern.channels.ErrorChannel
import co.nordprojects.lantern.shared.ChannelConfiguration
import co.nordprojects.lantern.shared.Direction
import org.json.JSONObject
import java.util.*

/**
 * The main activity coordinates the display of channels, depending on the current orientation
 * and the config.
 */
class MainActivity : Activity() {
    private val TAG = MainActivity::class.java.simpleName

    private val accelerometerObserver = Observer { _, _ -> accelerometerUpdated() }
    private val appConfigObserver = Observer { _, _ -> appConfigUpdated() }
    private val channels = mutableMapOf<Direction, Channel>()
    private var visibleChannel: Channel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.instance.accelerometer.addObserver(accelerometerObserver)
        App.instance.config.addObserver(appConfigObserver)

        // TODO(joerick): remove this! 🔥🔥🔥🔥
        App.instance.config.updateWithJson(JSONObject(
                """{
                    "planes": {
                        "up": {
                            "type": "bat-signal"
                        },
                        "forward": {
                            "type": "now-playing"
                        },
                        "down": {
                            "type": "webview",
                            "settings": {
                                "url": "https://youtu.be/6ZfuNTqbHE8",
                                "scrollTo": 30
                            }
                        }
                    }
                }"""))

        setContentView(R.layout.main_activity_layout)
        updateChannels()
        Log.d(TAG, "Main activity created.")
    }

    override fun onDestroy() {
        super.onDestroy()
        App.instance.accelerometer.deleteObserver(accelerometerObserver)
        App.instance.config.deleteObserver(appConfigObserver)
    }

    override fun onStart() {
        super.onStart()
        updateVisibleChannel()
        Log.d(TAG, "Main activity started.")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "Main activity stopped.")
    }

    private fun accelerometerUpdated() {
        Log.d(TAG, "accelerometer direction updated to ${App.instance.accelerometer.direction}")
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
                        (incomingChannelConfig != prevChannel.config)
                    }

            if (needsRefresh) {
                val newChannel = newChannelForConfig(incomingChannelConfig)
                channels[direction] = newChannel
                Log.i(TAG, "Channel for $direction is now $newChannel")
                updateVisibleChannel()
            }
        }
    }

    private fun updateVisibleChannel() {
        val newVisibleChannel = channels[App.instance.accelerometer.direction]
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
        var channel: Channel? = null

        for ((channelConstructor, info) in ChannelsRegistry.channelsWithInfo) {
            if (info.id == config.type) {
                channel = channelConstructor()
                break
            }
        }

        if (channel == null) {
            channel = ErrorChannel()
            args.putString(ErrorChannel.ARG_MESSAGE, "Unknown channel type '${config.type}'")
        }

        channel.arguments = args
        return channel
    }
}
