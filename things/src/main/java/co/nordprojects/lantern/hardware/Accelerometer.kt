package co.nordprojects.lantern.hardware

import android.os.Handler
import android.os.Looper
import android.util.Log
import co.nordprojects.lantern.shared.Direction
import co.nordprojects.lantern.util.Vector3D
import com.cacaosd.adxl345.ADXL345
import java.io.IOException
import java.util.*
import kotlin.concurrent.fixedRateTimer

/**
 * Tracks the gravity vector in the accelerometer and outputs a direction.
 *
 * Created by joerick on 22/01/18.
 */
class Accelerometer: Observable(), AutoCloseable {
    val TAG = this::class.java.simpleName

    val device = ADXL345(BoardDefaults.busForAccelerometer)
    var updateTimer: Timer? = null
    var direction: Direction? = null
        private set

    fun startUpdating() {
        if (updateTimer != null) {
            stopUpdating()
        }

        update()

        updateTimer = fixedRateTimer("Accelerometer", startAt = Date(), period = 200) {
            try {
                update()
            }
            catch (e: IOException) {
                Log.e(TAG, "Failed to update accelerometer.", e)
            }
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

    private val Direction.vector: Vector3D
        get() {
            return when (this) {
                Direction.UP -> Vector3D(0.0, 0.0, -1.0)
                Direction.FORWARD -> Vector3D(0.0, 1.0, 0.0)
                Direction.DOWN -> Vector3D(0.0, 0.0, 1.0)
            }
        }

    private fun Direction.alignmentWithVector(vector: Vector3D): Double {
        return this.vector.normalized().dot(vector.normalized())
    }
}