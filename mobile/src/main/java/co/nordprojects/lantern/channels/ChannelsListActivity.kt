package co.nordprojects.lantern.channels

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import co.nordprojects.lantern.App
import co.nordprojects.lantern.R
import co.nordprojects.lantern.shared.ChannelConfiguration
import co.nordprojects.lantern.shared.ChannelInfo
import co.nordprojects.lantern.shared.Direction
import kotlinx.android.synthetic.main.activity_channels_list.*
import org.json.JSONObject
import java.util.*

class ChannelsListActivity : AppCompatActivity(),
        ChannelListFragment.OnChannelSelectedListener {

    private var direction: Direction = Direction.FORWARD
    private var projectorObserver = Observer({_, _ -> projectorDidUpdate()})

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_channels_list)

        val directionString = intent.getStringExtra("direction")
        direction = Direction.valueOf(directionString)
        direction_text.text = direction.name

        supportFragmentManager.beginTransaction().apply {
            add(R.id.fragmentContainer, ChannelListFragment())
            commit()
        }
    }

    override fun onPause() {
        super.onPause()
        App.instance.projector?.deleteObserver(projectorObserver)
    }

    override fun onChannelSelected(channel: ChannelInfo) {
        val json = JSONObject().apply {
            put("type", channel.id)
        }
        val config = ChannelConfiguration(json)
        val connection = App.instance.configClient.activeConnection!!
        connection.sendSetPlane(direction, config)

        App.instance.projector?.addObserver(projectorObserver)
    }

    private fun projectorDidUpdate() {
        finish()
    }
}
