package co.nordprojects.lantern.channels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.nordprojects.lantern.Channel
import android.content.ContentResolver
import android.media.MediaPlayer
import android.media.SyncParams
import android.net.Uri
import android.util.Log
import android.widget.MediaController
import android.widget.VideoView
import co.nordprojects.lantern.R
import com.google.android.gms.awareness.Awareness


/**
 * Created by joerick on 13/02/18.
 */
class AmbientWeatherChannel : Channel() {
    val TAG = this::class.java.simpleName

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val videoView = VideoView(context)
        videoView.setVideoURI(Uri.parse("https://s3.amazonaws.com/lantern-resources/rain10_b.mp4"))
        videoView.start()
        videoView.setOnPreparedListener({
            it.isLooping = true
        })

        return videoView
    }
}
