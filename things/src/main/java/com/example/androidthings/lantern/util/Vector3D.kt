package com.example.androidthings.lantern.util

import kotlin.math.sqrt

/**
 * Represents a vector in 3D space.
 *
 * Created by joerick on 22/01/18.
 */
data class Vector3D(val x: Double, val y: Double, val z: Double) {
    val length: Double
        get() = sqrt(x*x + y*y + z*z)

    fun normalized(): Vector3D {
        return Vector3D(x / length, y / length, z / length)
    }

    fun dot(other: Vector3D): Double {
        return x*other.x + y*other.y + z*other.z
    }
}