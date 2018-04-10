package com.example.androidthings.lantern

import android.location.Location
import com.example.androidthings.lantern.shared.Direction
import kotlin.math.absoluteValue

/**
 * Specifies a highlight color used throughout the app for each direction.
 */
val Direction.color: Int get() {
    return when(this) {
        Direction.UP -> R.color.upPlane
        Direction.FORWARD -> R.color.forwardPlane
        Direction.DOWN -> R.color.downPlane
    }
}

/**
 * Converts Location.FORMAT_SECONDS to Decimal Minutes Seconds.
 * e.g. 40:42:51.26472 to 40°42′51.2647″ N
 * Adapted from https://stackoverflow.com/a/39055801/2546065
 */
object LocationConverter {

    fun latitudeAsDMS(latitude: Double, decimalPlace: Int): String {
        val direction = if (latitude > 0) "N" else "S"
        var strLatitude = Location.convert(latitude.absoluteValue, Location.FORMAT_SECONDS)
        strLatitude = replaceDelimiters(strLatitude, decimalPlace)
        strLatitude += " $direction"
        return strLatitude
    }

    fun longitudeAsDMS(longitude: Double, decimalPlace: Int): String {
        val direction = if (longitude > 0) "W" else "E"
        var strLongitude = Location.convert(longitude.absoluteValue, Location.FORMAT_SECONDS)
        strLongitude = replaceDelimiters(strLongitude, decimalPlace)
        strLongitude += " $direction"
        return strLongitude
    }

    private fun replaceDelimiters(str: String, decimalPlace: Int): String {
        var string = str
        string = string.replaceFirst(":".toRegex(), "°")
        string = string.replaceFirst(":".toRegex(), "'")
        val pointIndex = string.indexOf(".")
        val endIndex = pointIndex + 1 + decimalPlace
        if (endIndex < str.length) {
            string = string.substring(0, endIndex)
        }
        string += "\""
        return string
    }
}
