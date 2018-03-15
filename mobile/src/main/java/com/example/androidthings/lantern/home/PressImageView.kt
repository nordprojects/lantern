package com.example.androidthings.lantern.home

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageButton

/**
 * Created by Michael Colville on 15/03/2018.
 */
class PressImageView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ImageButton(context, attrs, defStyleAttr) {

    var onPressedChangedListener: ((Boolean) -> Unit)? = null

    override fun setPressed(pressed: Boolean) {
        super.setPressed(pressed)
        onPressedChangedListener?.invoke(pressed)
    }
}
