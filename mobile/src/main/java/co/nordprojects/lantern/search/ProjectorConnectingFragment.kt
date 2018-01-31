package co.nordprojects.lantern.search


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.nordprojects.lantern.R


/**
 * A simple [Fragment] subclass.
 */
class ProjectorConnectingFragment : Fragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_projector_connecting, container, false)
    }

}// Required empty public constructor
