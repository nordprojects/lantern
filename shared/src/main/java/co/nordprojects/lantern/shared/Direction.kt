package co.nordprojects.lantern.shared

/**
 * Created by joerick on 25/01/18.
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
