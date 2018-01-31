package co.nordprojects.lantern.channels

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import co.nordprojects.lantern.R
import co.nordprojects.lantern.shared.Direction
import kotlinx.android.synthetic.main.activity_channels_list.*

class ChannelsListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_channels_list)

        val directionString = intent.getStringExtra("direction")
        val direction = Direction.valueOf(directionString)
        direction_text.text = direction.name
    }
}
