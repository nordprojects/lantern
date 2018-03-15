package com.example.androidthings.lantern.home


import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import com.example.androidthings.lantern.App
import com.example.androidthings.lantern.R
import com.example.androidthings.lantern.color
import com.example.androidthings.lantern.configuration.ProjectorState
import com.example.androidthings.lantern.shared.Direction
import kotlinx.android.synthetic.main.fragment_projector_display.*


class ProjectorDisplayFragment : Fragment() {

    private var directionSelectedListener: OnDirectionSelectedListener? = null
    private var planeViews: Map<Direction, TextView> = mapOf()
    private var lampGlowViews: Map<Direction, ImageView> = mapOf()
    private var marqueeHandler = Handler()

    var projector: ProjectorState? = null
        set(value) {
            field = value
            update()
        }

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

        lampGlowViews = mapOf(
                Direction.UP to lampGlowUp,
                Direction.FORWARD to lampGlowForward,
                Direction.DOWN to lampGlowDown
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

        up_button.onPressedChangedListener = { pressed ->
            val offset = (4.5F / 34F) * up_button.height.toFloat()
            lampGlowUp.translationY = if (pressed) -offset else 0F
        }

        forward_button.onPressedChangedListener = { pressed ->
            val offset = (4.5F / 34F) * forward_button.width.toFloat()
            lampGlowForward.translationX = if (pressed) offset else 0F
        }

        down_button.onPressedChangedListener = { pressed ->
            val offset = (4.5F / 34F) * down_button.height.toFloat()
            lampGlowDown.translationY = if (pressed) offset else 0F
        }

        update()
    }

    private fun update() {
        if (view == null) return
        val projector = projector ?: return
        val connection = App.instance.client.activeConnection

        // Channel Labels
        for (direction in Direction.values()) {
            val channel = projector.planes[direction]!!
            val view = planeViews[direction]!!
            val channelInfo = connection?.channelInfoForChannelType(channel.type)
            val name = channelInfo?.name ?: channel.type
            view.text = "‘$name’"
        }

        // Projector head
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

        // Lamp glow
        for (each in lampGlowViews) {
            val glowDirection = each.key
            val glowView = each.value
            val alpha = if (glowDirection == projector.direction) 1.0F else 0.0F
            glowView.animate().apply {
                duration = 300
            }.alpha(alpha)
        }

        // Current channel title & subtitle
        val channel = projector.planes[projector.direction]
        if (channel != null) {
            val channelInfo = App.instance.client.activeConnection?.channelInfoForChannelType(channel.type)
            val color = ContextCompat.getColor(context!!, projector.direction.color)
            currentChannelNameTextView.text = channelInfo?.name ?: ""
            currentChannelNameTextView.setTextColor(color)

            val subtitle = channel.settings.optString("subtitle", "")
            val via = channel.settings.optString("subtitleVia", "")

            val text = if (via == "") subtitle else "$subtitle ${via.toUpperCase()}"
            val spannable = SpannableString(text)
            spannable.setSpan(AbsoluteSizeSpan(13, true), 0, subtitle.length, 0)
            if (via != "") {
                spannable.setSpan(AbsoluteSizeSpan(12, true),
                        subtitle.length + 1,
                        text.length,
                        0)
            }

            currentChannelSubtitleTextView.text = spannable
            currentChannelSubtitleTextView.setTextColor(color)
            currentChannelSubtitleTextView.isSelected = false
            marqueeHandler.removeCallbacksAndMessages(null)
            marqueeHandler.postDelayed( {
                if (currentChannelSubtitleTextView != null) {
                    currentChannelSubtitleTextView.isSelected = true
                }
            }, 1000)

        }
    }
}
