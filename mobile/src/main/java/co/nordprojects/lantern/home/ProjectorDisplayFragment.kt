package co.nordprojects.lantern.home


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import co.nordprojects.lantern.App

import co.nordprojects.lantern.R
import co.nordprojects.lantern.shared.Direction
import kotlinx.android.synthetic.main.fragment_projector_display.*
import java.util.Observer


class ProjectorDisplayFragment : Fragment() {

    private var directionSelectedListener: OnDirectionSelectedListener? = null
    private val projectorConfigObserver = Observer { _, _ -> projectorConfigUpdated() }
    private var planeViews: Map<Direction, TextView> = mapOf()

    interface OnDirectionSelectedListener {
        fun onDirectionSelected(direction: Direction)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_projector_display, container, false)

        val activity = activity
        if (activity is OnDirectionSelectedListener) directionSelectedListener = activity

        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        planeViews = mapOf(
                Direction.UP to upTextView,
                Direction.FORWARD to forwardTextView,
                Direction.DOWN to downTextView
        )

        up_button.setOnClickListener {
            directionSelectedListener?.onDirectionSelected(Direction.UP)
        }
        forward_button.setOnClickListener {
            directionSelectedListener?.onDirectionSelected(Direction.FORWARD)
        }
        down_button.setOnClickListener {
            directionSelectedListener?.onDirectionSelected(Direction.DOWN)
        }

        update()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        App.instance.projector?.addObserver(projectorConfigObserver)
    }

    override fun onDestroy() {
        super.onDestroy()

        App.instance.projector?.deleteObserver(projectorConfigObserver)
    }

    fun projectorConfigUpdated() {
        update()
    }

    fun update() {

        for (direction in Direction.values()) {
            val channel = App.instance.projector!!.planes[direction]!!
            val view = planeViews[direction]!!

            val channelInfo = App.instance.projector!!.channelInfoForChannelType(channel.type)

            if (channelInfo == null) {
                view.text = channel.type
            } else {
                view.text = channelInfo.name
            }
        }

        val projectorHeadAngle: Float = when(App.instance.projector?.direction) {
            Direction.UP -> -90F
            Direction.FORWARD -> 0F
            Direction.DOWN -> 90F
            null -> 0F
        }

        projectorHeadImageView.animate().apply {
            interpolator = AccelerateDecelerateInterpolator()
            duration = 300
        }.rotation(projectorHeadAngle)
    }
}
