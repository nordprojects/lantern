package com.example.androidthings.lantern.search


import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.LinearInterpolator
import com.example.androidthings.lantern.R
import kotlinx.android.synthetic.main.fragment_projector_search.*

class ProjectorSearchFragment : Fragment() {

    private var searchAnimatorSet: AnimatorSet? = null
    private var errorAnimatorSet: AnimatorSet? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_projector_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showSearch()
    }

    fun showSearch() {
        startSearchAnimation()
        textView.text = "Searching for nearby Lanterns…"
    }

    fun showError() {
        startErrorAnimation()
        textView.text = "Oh no!\n" +
                "We couldn’t find any nearby Lanterns. Check the one you’re trying to connect to has power and is on the WiFi."
    }

    private fun startSearchAnimation() {
        stopAnimations()
        lampHead.rotation = 45F
        val animations = listOf(0F, -45F, -135F, -180F, -270F, -315F).map {
            ObjectAnimator.ofFloat(lampHead, "rotation", it).apply {
                duration = 500
                startDelay = 500
            }
        }
        val animatorSet = AnimatorSet().apply {
            playSequentially(animations)
        }
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                if (view != null) {
                    animation.start()
                }
            }
        })
        animatorSet.start()
        searchAnimatorSet = animatorSet
    }

    private fun startErrorAnimation() {
        stopAnimations()

        val returnAnim = ObjectAnimator.ofFloat(lampHead, "rotation", -280F).apply {
            duration = 500
            interpolator = LinearInterpolator()
        }

        val fallAnim = ObjectAnimator.ofFloat(teardrop, "translationY", 330F).apply {
            duration = 1100
            interpolator = AccelerateInterpolator()
        }

        val squashAnim = ObjectAnimator.ofFloat(teardrop, "scaleY", 0F).apply {
            startDelay = 1000
            duration = 200
        }

        val animatorSet = AnimatorSet().apply {
            playTogether(fallAnim, squashAnim)
        }

        // Start teardrop anim after returning lamp head to position
        returnAnim.addListener(object: AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                if (view != null) {
                    animatorSet.start()
                }
            }
        })

        // Keep teardrop animating
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                if (view != null) {
                    animation.start()
                }
            }
        })

        returnAnim.start()
        errorAnimatorSet = animatorSet
    }

    private fun stopAnimations() {
        searchAnimatorSet?.removeAllListeners()
        searchAnimatorSet?.cancel()

        errorAnimatorSet?.removeAllListeners()
        errorAnimatorSet?.cancel()
    }
}
