package co.nordprojects.lantern.home


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import co.nordprojects.lantern.App

import co.nordprojects.lantern.R
import co.nordprojects.lantern.channels.color
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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

    private fun projectorConfigUpdated() {
        update()
    }

    private fun update() {
        val projector = App.instance.projector ?: return
        for (direction in Direction.values()) {

            val channel = projector.planes[direction]!!
            val view = planeViews[direction]!!

            val channelInfo = projector.channelInfoForChannelType(channel.type)

            if (channelInfo == null) {
                view.text = channel.type
            } else {
                view.text = "‘${channelInfo.name}’"
            }
        }

        val projectorHeadAngle: Float = when(projector.direction) {
            Direction.UP -> -90F
            Direction.FORWARD -> 0F
            Direction.DOWN -> 90F
        }

        val projectorHeadImage: Int = when(projector.direction) {
            Direction.UP -> R.drawable.projector_head_up
            Direction.FORWARD -> R.drawable.projector_head_forward
            Direction.DOWN -> R.drawable.projector_head_down
        }

        projectorHeadImageView.animate().apply {
            interpolator = AccelerateDecelerateInterpolator()
            duration = 300
        }.rotation(projectorHeadAngle)

        projectorHeadImageView.setImageResource(projectorHeadImage)

        val channel = projector.planes[projector.direction]
        if (channel != null) {
            val channelInfo = projector.channelInfoForChannelType(channel.type)
            val color = ContextCompat.getColor(context!!, projector.direction.color)
            currentChannelNameTextView.text = channelInfo?.name ?: ""
            currentChannelNameTextView.setTextColor(color)

            val subtitle = channel.settings.optString("subtitle", "")
            currentChannelSubtitleTextView.text = subtitle
            currentChannelSubtitleTextView.setTextColor(color)
        }
    }
}
