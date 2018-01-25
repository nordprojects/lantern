package co.nordprojects.lantern.shared

/**
 * Created by joerick on 25/01/18.
 */
enum class Direction {
    UP {
        override val jsonName = "up"
    },
    FORWARD {
        override val jsonName = "forward"
    },
    DOWN {
        override val jsonName = "down"
    };

    abstract val jsonName: String
    companion object {
        fun withJsonName(jsonName: String): Direction {
            return Direction.values().find { it.jsonName == jsonName } ?:
                    throw IllegalArgumentException("No Direction with jsonName $jsonName")
        }
    }
}
