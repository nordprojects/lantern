package com.example.androidthings.lantern

import com.example.androidthings.lantern.shared.Direction

val Direction.color: Int get() {
    return when(this) {
        Direction.UP -> R.color.upPlane
        Direction.FORWARD -> R.color.forwardPlane
        Direction.DOWN -> R.color.downPlane
    }
}