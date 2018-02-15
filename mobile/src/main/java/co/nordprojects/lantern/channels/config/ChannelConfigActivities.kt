package co.nordprojects.lantern.channels.config


/**
 * Created by Michael Colville on 12/02/2018.
 */
object ChannelConfigActivities {
    val channelTypeToActivity = mapOf(
            "message" to MessageConfigActivity::class.java,
            "calendar-clock" to CalendarConfigActivity::class.java
    )
}
