package co.nordprojects.lantern

import android.os.Handler
import android.os.Looper
import com.cacaosd.adxl345.ADXL345
import java.util.*
import kotlin.concurrent.fixedRateTimer

/**
 * Tracks the gravity vector in the accelerometer and outputs a direction.
 *
 * Created by joerick on 22/01/18.
 */
enum class Direction {
    UP {
        override val vector: Vector3D
            get() = Vector3D(0.0, 0.0, -1.0)
    },
    FORWARD {
        override val vector: Vector3D
            get() = Vector3D(0.0, 1.0, 0.0)
    },
    DOWN {
        override val vector: Vector3D
            get() = Vector3D(0.0, 0.0, 1.0)
    };

    abstract val vector: Vector3D
    fun alignmentWithVector(vector: Vector3D): Double {
        return this.vector.normalized().dot(vector.normalized())
    }
}

class Accelerometer: Observable(), AutoCloseable {
    val device = ADXL345(BoardDefaults.busForAccelerometer)
    var updateTimer: Timer? = null
    var direction: Direction? = null
        private set

    fun startUpdating() {
        if (updateTimer != null) {
            stopUpdating()
        }
        updateTimer = fixedRateTimer("Accelerometer", startAt = Date(), period = 200) {
            update()
        }
    }

    fun stopUpdating() {
        updateTimer?.cancel()
        updateTimer = null
    }

    fun update() {
        // range is 10 bits, covering 2g of acceleration
        val scaleFactor = 2.0/1024.0

        val acceleration = Vector3D(
                device.accelerationX * scaleFactor,
                device.accelerationY * scaleFactor,
                device.accelerationZ * scaleFactor
        )

        // the directions are ranked by alignment to the acceleration vector.
        // scores are from -1.0 to 1.0, with 1.0 being exactly aligned.
        val directionsWithScore = Direction.values().map {
            Pair(it, it.alignmentWithVector(acceleration))
        }
        val (bestDirection, bestDirectionScore) = directionsWithScore.sortedBy { it.second }.last()

        // if the best direction has a score greater than 0.8, change the direction property
        if (this.direction == null
                || (bestDirection != this.direction && bestDirectionScore > 0.8)) {
            this.direction = bestDirection
            setChanged()

            // notify observers on the main thread
            Handler(Looper.getMainLooper()).post {
                notifyObservers()
            }
        }
    }

    override fun close() {
        stopUpdating()
        device.close()
    }
}