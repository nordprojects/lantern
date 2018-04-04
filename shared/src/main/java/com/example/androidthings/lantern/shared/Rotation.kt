package com.example.androidthings.lantern.shared

/**
 * Created by Michael Colville on 03/04/2018.
 */

enum class Rotation {
    LANDSCAPE, LANDSCAPE_UPSIDE_DOWN;

    val jsonName: String get() {
        return when (this) {
            LANDSCAPE -> "landscape"
            LANDSCAPE_UPSIDE_DOWN -> "landscape_upside_down"
        }
    }

    companion object {
        fun withJsonName(jsonName: String): Rotation {
            return Rotation.values().find { it.jsonName == jsonName } ?:
            throw IllegalArgumentException("No Direction with jsonName $jsonName")
        }
    }
}
