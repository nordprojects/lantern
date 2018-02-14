package co.nordprojects.lantern.search


import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import co.nordprojects.lantern.R
import kotlinx.android.synthetic.main.fragment_projector_search.*

class ProjectorSearchFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_projector_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startSearchAnimation()
    }

    private fun startSearchAnimation() {
        lampHead.rotation = 45F
        val animations = listOf(0F, -45F, -135F, -180F, -270F, -315F).map {
            ObjectAnimator.ofFloat(lampHead, "rotation", it).apply {
                duration = 500
                startDelay = 500
                interpolator = AccelerateDecelerateInterpolator()
            }
        }
        val animatorSet = AnimatorSet().apply {
            playSequentially(animations)
        }
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                animation.start()
            }
        })
        animatorSet.start()
    }
}
