package co.nordprojects.lantern.search


import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.nordprojects.lantern.R
import kotlinx.android.synthetic.main.fragment_projector_select.*


class ProjectorSelectFragment : Fragment() {

    private var mCallback: OnProjectorSelectedListener? = null

    interface OnProjectorSelectedListener {
        fun onProjectorSelected(position: Int)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_projector_select, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button2.setOnClickListener { onProjectorClick(button2) }
        button3.setOnClickListener { onProjectorClick(button3) }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        val activity = activity
        if (activity is OnProjectorSelectedListener) mCallback = activity
    }

    private fun onProjectorClick(view: View) {
        mCallback?.onProjectorSelected(0)
    }
}
