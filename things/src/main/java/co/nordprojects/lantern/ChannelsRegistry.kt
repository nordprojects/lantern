package co.nordprojects.lantern

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Base64OutputStream
import co.nordprojects.lantern.channels.*
import co.nordprojects.lantern.channels.nowplaying.NowPlayingChannel
import co.nordprojects.lantern.shared.ChannelInfo
import java.io.ByteArrayOutputStream
import java.io.IOException


/**
 * Stores a the available channels with corresponding info for them.
 *
 * Created by joerick on 25/01/18.
 */
object ChannelsRegistry {
    val channelsWithInfo = arrayOf<Pair<() -> Channel, ChannelInfo>>(
            Pair(::CalendarChannel, ChannelInfo(
                    "calendar-clock",
                    "Calendar Clock",
                    "Augments a real-world clock with your appointments from Google Calendar",
                    dataUriForDrawableResource(R.drawable.calendar_clock_icon)
            )),
            Pair(::NowPlayingChannel, ChannelInfo(
                    "now-playing",
                    "Now Playing",
                    "Displays the track currently playing on your Cast-enabled speaker",
                    dataUriForDrawableResource(R.drawable.now_playing_icon),
                    customizable = true
            )),
            Pair(::LampChannel, ChannelInfo(
                    "lamp",
                    "Spotlight",
                    "Bring a little drama to your desk."
            )),
            Pair(::BlankChannel, ChannelInfo(
                    "blank",
                    "Blank",
                    "Show nothing in this direction."
            )),
            Pair(::BatSignalChannel, ChannelInfo(
                    "bat-signal",
                    "The Bat Signal",
                    "Summons the caped crusader to your location, as long as you're in Gotham. Only use in an emergency!"
            )),
            Pair(::SpaceChannel, ChannelInfo(
                    "space",
                    "Space Porthole",
                    "See the stars above you"
            )),
            Pair(::MessageChannel, ChannelInfo(
                    "message",
                    "Message",
                    "Display a short message",
                    customizable = true
            )),
            Pair(::WebViewChannel, ChannelInfo(
                    "webview",
                    "Web View",
                    "Show any web page you want to explore."
            ))
    )

    val channelsInfo = channelsWithInfo.map { it.second }

    fun dataUriForDrawableResource(resourceId: Int): Uri {
        var bitmap = BitmapFactory.decodeResource(App.instance.resources, resourceId)
        val outputStream = ByteArrayOutputStream()
        val base64Stream = Base64OutputStream(outputStream, Base64.DEFAULT)

        // resize the image to 256x256
        bitmap = Bitmap.createScaledBitmap(bitmap, 256, 256, true)

        // write data uri header
        outputStream.write("data:image/webp;base64,".toByteArray())

        // write base64-encoded webp data
        val success = bitmap.compress(
                Bitmap.CompressFormat.WEBP,
                20,
                base64Stream)
        base64Stream.flush()

        if (!success) {
            throw IOException("image encoding failed")
        }

        return Uri.parse(outputStream.toString())
    }
}


