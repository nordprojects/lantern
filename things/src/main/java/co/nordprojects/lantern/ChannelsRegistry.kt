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
                    "Augmented clock",
                    "Projects your google calendar events around a real-world clock",
                    Uri.parse("android.resource://co.nordprojects.lantern/drawable/banner_clock"),
                    customizable = true
            )),
            Pair(::NowPlayingChannel, ChannelInfo(
                    "now-playing",
                    "Now playing",
                    "Displays song information for whatever's playing through your Cast-enabled speaker",
                    Uri.parse("android.resource://co.nordprojects.lantern/drawable/banner_now_playing"),
                    customizable = true
            )),
            Pair(::AmbientWeatherChannel, ChannelInfo(
                    "ambient-weather",
                    "Weather caustics",
                    "Ambient water reflections react to Google weather data for a chosen location.",
                    Uri.parse("android.resource://co.nordprojects.lantern/drawable/banner_weather")
                    )),
            Pair(::SpaceChannel, ChannelInfo(
                    "space",
                    "Space porthole",
                    "Explore the galaxy with this virtual telescope. Look out for the ISS!",
                    Uri.parse("android.resource://co.nordprojects.lantern/drawable/banner_space")
            )),
            Pair(::LampChannel, ChannelInfo(
                    "lamp",
                    "Spotlight",
                    "Bring a little drama to your desktop",
                    Uri.parse("android.resource://co.nordprojects.lantern/drawable/banner_spotlight")
            )),
            Pair(::WebViewChannel, ChannelInfo(
                    "webview",
                    "Web view",
                    "Load a URL for your favourite site, a recipe or a fun web app you’re building",
                    Uri.parse("android.resource://co.nordprojects.lantern/drawable/banner_web_page"),
                    customizable = true
            )),
            Pair(::BlankChannel, ChannelInfo(
                    "blank",
                    "Blank",
                    "Kill projections for this direction",
                    Uri.parse("android.resource://co.nordprojects.lantern/drawable/banner_blank")
            )),
            Pair(::BatSignalChannel, ChannelInfo(
                    "bat-signal",
                    "Bat-Signal",
                    "Summon the Batman when troubles about",
                    Uri.parse("android.resource://co.nordprojects.lantern/drawable/banner_bat_signal")
            )),
            Pair(::MessageChannel, ChannelInfo(
                    "message",
                    "Message",
                    "Display a short message",
                    Uri.parse("android.resource://co.nordprojects.lantern/drawable/banner_blank"),
                    customizable = true
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
