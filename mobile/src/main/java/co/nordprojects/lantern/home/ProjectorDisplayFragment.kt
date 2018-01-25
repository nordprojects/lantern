package co.nordprojects.lantern.home


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

import co.nordprojects.lantern.R
import kotlinx.android.synthetic.main.fragment_projector_display.*


class ProjectorDisplayFragment : Fragment() {

    private var mCallback: OnDirectionSelectedListener? = null

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

        up_button.setOnClickListener { mCallback?.onDirectionSelected("up") }
        forward_button.setOnClickListener { mCallback?.onDirectionSelected("forward")}
        down_button.setOnClickListener { mCallback?.onDirectionSelected("down")}
    }

}// Required empty public constructor
