package com.example.androidthings.lantern.shared

/**
 * Represents the directions that the Lantern can project a different channel.
 */
enum class Direction {
    UP, FORWARD, DOWN;

    val jsonName: String get() {
        return when (this) {
            UP -> "up"
            FORWARD -> "forward"
            DOWN -> "down"
        }
    }

    companion object {
        fun withJsonName(jsonName: String): Direction {
            return Direction.values().find { it.jsonName == jsonName } ?:
                    throw IllegalArgumentException("No Direction with jsonName $jsonName")
        }
    }
}
