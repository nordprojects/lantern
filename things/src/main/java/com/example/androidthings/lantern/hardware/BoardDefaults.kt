package com.example.androidthings.lantern.hardware

import android.os.Build

/**
 * Holds the hardware-specific parameters of the app.
 */
object BoardDefaults {
    private val DEVICE_RPI3 = "rpi3"

    /**
     * Return the I2C bus that the accelerometer can be accessed on
     */
    val busForAccelerometer: String
        get() {
            when (Build.DEVICE) {
                DEVICE_RPI3 -> return "I2C1"
                else -> throw IllegalStateException("Unsupported Build.DEVICE " + Build.DEVICE)
            }
        }
}
