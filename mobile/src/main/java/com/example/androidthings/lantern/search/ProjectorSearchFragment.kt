package com.example.androidthings.lantern.search


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
import android.view.animation.AccelerateInterpolator
import android.view.animation.LinearInterpolator
import com.example.androidthings.lantern.R
import kotlinx.android.synthetic.main.fragment_projector_search.*

class ProjectorSearchFragment : Fragment() {

    private var searchAnimatorSet: AnimatorSet? = null
    private var errorAnimatorSet: AnimatorSet? = null

    private var listener: SearchFragmentListener? = null

    interface SearchFragmentListener {
        fun onTryAgainClicked()
        fun onSettingsClicked()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_projector_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showSearch()
        tryAgainButton.setOnClickListener { listener?.onTryAgainClicked() }
        settingsButton.setOnClickListener { listener?.onSettingsClicked() }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        val activity = activity
        if (activity is SearchFragmentListener) listener = activity

    }

    fun showSearch() {
        if (view == null) return
        startSearchAnimation()
        textView.text = "Searching for nearby Lanterns…"
        tryAgainButton.visibility = View.INVISIBLE
        settingsButton.visibility = View.INVISIBLE
    }

    fun showTimeoutError() {
        if (view == null) return
        startErrorAnimation()
        textView.text = "Oh no!\nWe couldn’t find any nearby Lanterns. Check the one you’re trying to connect to has power and is close by."
        tryAgainButton.visibility = View.VISIBLE
        settingsButton.visibility = View.INVISIBLE
    }

    fun showPermissionsError() {
        if (view == null) return
        startErrorAnimation()
        textView.text = "Failed to start Nearby.\n\nMake sure you’ve enabled Location permissions in settings"
        tryAgainButton.visibility = View.INVISIBLE
        settingsButton.visibility = View.VISIBLE
    }

    fun showBluetoothError() {
        if (view == null) return
        startErrorAnimation()
        textView.text = "Failed to start Nearby.\n\nCheck Bluetooth is turned on and try again."
        tryAgainButton.visibility = View.VISIBLE
        settingsButton.visibility = View.INVISIBLE
    }

    fun showUnknownError() {
        if (view == null) return
        startErrorAnimation()
        textView.text = "Failed to start Nearby.\n\nSomething went wrong, please try again."
        tryAgainButton.visibility = View.VISIBLE
        settingsButton.visibility = View.INVISIBLE
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
        val fallDistance: Float = lampBody.height.toFloat() * 0.69F

        val returnAnim = ObjectAnimator.ofFloat(lampHead, "rotation", -280F).apply {
            duration = 500
            interpolator = LinearInterpolator()
        }

        val fallAnim = ObjectAnimator.ofFloat(teardrop, "translationY", fallDistance).apply {
            duration = 1100
            interpolator = AccelerateInterpolator()
        }

        val squashAnim = ObjectAnimator.ofFloat(teardrop, "scaleY", 0F).apply {
            startDelay = 1000
            duration = 200
        }

        val resetAnim = ObjectAnimator.ofFloat(teardrop, "translationY", 0F).apply {
            startDelay = 1300
            duration = 0
        }

        val resetScaleAnim = ObjectAnimator.ofFloat(teardrop, "scaleY", 1F).apply {
            startDelay = 1300
            duration = 0
        }

        val animatorSet = AnimatorSet().apply {
            playTogether(fallAnim, squashAnim, resetAnim, resetScaleAnim)
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

        teardrop.scaleY = 1F
        teardrop.translationY = 0F
    }
}
