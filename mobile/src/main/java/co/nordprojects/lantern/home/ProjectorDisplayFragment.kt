package co.nordprojects.lantern.home


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import co.nordprojects.lantern.App

import co.nordprojects.lantern.R
import co.nordprojects.lantern.shared.Direction
import kotlinx.android.synthetic.main.fragment_projector_display.*
import java.util.Observer


class ProjectorDisplayFragment : Fragment() {

    private var mCallback: OnDirectionSelectedListener? = null
    private val projectorConfigObserver = Observer { _, _ -> projectorConfigUpdated() }

    interface OnDirectionSelectedListener {
        fun onDirectionSelected(direction: String)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_projector_display, container, false)

        val activity = activity
        if (activity is OnDirectionSelectedListener) mCallback = activity

        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        up_button.setOnClickListener { update() }

       // up_button.setOnClickListener { mCallback?.onDirectionSelected("up") }
        forward_button.setOnClickListener { mCallback?.onDirectionSelected("forward")}
        down_button.setOnClickListener { mCallback?.onDirectionSelected("down")}

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        App.instance.configClient.activeConnection?.projectorConfig?.addObserver(projectorConfigObserver)
    }

    override fun onDestroy() {
        super.onDestroy()

        App.instance.configClient.activeConnection!!.projectorConfig.deleteObserver(projectorConfigObserver)
    }
    fun projectorConfigUpdated() {
        update()
    }

    fun update() {
        val config = App.instance.configClient.activeConnection!!.projectorConfig
        forwardTextView.text = config.planes[Direction.FORWARD]?.type

        directionTextView.text = config.direction.name
    }
}
