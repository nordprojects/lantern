package co.nordprojects.lantern.channels

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.nordprojects.lantern.Channel
import kotlin.math.min


/**
 * Projects a perfect spotlight circle.
 */
class LampChannel : Channel() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return object : View(context) {
            private val circlePaint = Paint().apply {
                color = Color.WHITE
            }

            override fun onDraw(canvas: Canvas) {
                super.onDraw(canvas)

                val width: Float = canvas.width.toFloat()
                val height: Float = canvas.height.toFloat()

                canvas.drawColor(Color.BLACK)

                canvas.drawCircle(
                        width/2,
                        height/2,
                        min(width/2, height/2),
                        circlePaint
                )
            }
        }
    }
}
