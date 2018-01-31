package co.nordprojects.lantern.home


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
            view.text = channel.type
        }

        directionTextView.text = App.instance.projector!!.direction.name
    }
}
