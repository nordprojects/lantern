package com.example.androidthings.lantern.connect

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.animation.LinearInterpolator
import com.example.androidthings.lantern.R
import kotlinx.android.synthetic.main.fragment_projector_connecting.*

class ProjectorConnectingFragment : Fragment() {

    val name: String by lazy {arguments?.getString(ConnectActivity.ARG_NAME) ?: "Lantern" }
    var animatorSet: AnimatorSet? = null

    private var listener: ConnectingFragmentListener? = null

    interface ConnectingFragmentListener {
        fun onTryAgainClicked()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_projector_connecting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tryAgainButton.setOnClickListener { listener?.onTryAgainClicked() }
    }

    override fun onResume() {
        super.onResume()
        showConnecting()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        val activity = activity
        if (activity is ConnectingFragmentListener) listener = activity
    }

    fun showConnecting() {
        if (view == null) return
        rainbow.visibility = View.VISIBLE
        questionMark.visibility = View.INVISIBLE
        tryAgainButton.visibility = View.INVISIBLE
        startConnectingAnimation()
        connectingTextView.text = "Connecting to ‘$name’"
    }

    fun showError() {
        if (view == null) return
        stopAnimation()
        rainbow.visibility = View.INVISIBLE
        questionMark.visibility = View.VISIBLE
        tryAgainButton.visibility = View.VISIBLE
        connectingTextView.text = "Failed to connect to ‘$name’"
    }

    private fun startConnectingAnimation() {
        animatorSet?.removeAllListeners()
        animatorSet?.cancel()
        val halfDuration: Long = 500

        phone_lamp.measure(WRAP_CONTENT, WRAP_CONTENT)
        val gapWidth: Float = phone_lamp.measuredWidth.toFloat() * 0.5F

        rainbow.scaleX = 0.0F
        rainbow.translationX = 0.0F

        // GROW
        val grow = ObjectAnimator.ofFloat(rainbow, "scaleX", 4.0f).apply {
            duration = halfDuration
            interpolator = LinearInterpolator()
        }
        val translate = ObjectAnimator.ofFloat(rainbow, "translationX", gapWidth * 0.5F).apply {
            duration = halfDuration
            interpolator = LinearInterpolator()
        }
        val growSet = AnimatorSet().apply {
            playTogether(grow, translate)
        }

        // SHRINK
        val shrink = ObjectAnimator.ofFloat(rainbow, "scaleX", 0.0f).apply {
            duration = halfDuration
            interpolator = LinearInterpolator()
        }
        val translate2 = ObjectAnimator.ofFloat(rainbow, "translationX", gapWidth).apply {
            duration = halfDuration
            interpolator = LinearInterpolator()
        }
        val shrinkSet = AnimatorSet().apply {
            playTogether(shrink, translate2)
        }

        // COMBINED
        val fullSet = AnimatorSet().apply {
            playSequentially(growSet, shrinkSet)
        }
        fullSet.start()
        fullSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                if (view != null) {
                    animation.start()
                }
            }
        })
        animatorSet = fullSet
    }

    private fun stopAnimation() {
        animatorSet?.removeAllListeners()
        animatorSet?.cancel()
    }
}
